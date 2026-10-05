package er.plugintest.tests;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSForwardException;

import er.erxtest.EditingContextTracker;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * Base class for the database plugin tests salvaged from Wonder's PluginTest application.
 * They run against the database named by the system property {@code plugintest.database}:
 * {@code h2}, the default, for an in-memory H2 database, or {@code postgresql}, for a
 * PostgreSQL server that Testcontainers runs in Docker. Without Docker, the PostgreSQL
 * tests are skipped.
 * <p>
 * EOF can be set up only once in a JVM, so each database has a Surefire execution, and a
 * JVM, of its own. EOF is set up as in an app, through {@link ERXExtensions}, with the
 * settings from PluginTest's Properties. The tables are created when a test first needs
 * them.
 *
 * @since 0.3
 */
public abstract class PluginTestCase {
	public static final Logger log = Logger.getLogger(PluginTestCase.class);

	/**
	 * The name of the model the tests use.
	 */
	public static final String MODEL_NAME = "PluginTest";

	/**
	 * The database the tests run against.
	 */
	protected static final String DATABASE = System.getProperty("plugintest.database", "h2");

	/**
	 * The image the PostgreSQL tests run on.
	 */
	private static final String POSTGRESQL_IMAGE = "postgres:18-alpine";

	private static boolean _isSetUp;

	private static boolean _hasSchema;

	protected EOModel model;

	/**
	 * The database's name for {@link ERXSQLHelper#newSQLHelper(String)}.
	 */
	protected String adaptorName = DATABASE;

	/**
	 * Hands out and disposes of the test's editing contexts.
	 */
	private final EditingContextTracker editingContexts = new EditingContextTracker();

	/**
	 * Sets up EOF, the first time it's called in a JVM.
	 */
	@BeforeAll
	public static synchronized void setUpDatabase() {
		if (!_isSetUp) {
			String url;
			String username;
			String password;
			String plugin;
			switch (DATABASE) {
			case "h2" -> {
				url = "jdbc:h2:mem:plugintest;DB_CLOSE_DELAY=-1";
				username = "sa";
				password = "";
				plugin = "H2";
			}
			case "postgresql" -> {
				assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker isn't available");
				// Testcontainers' Ryuk container stops it when the JVM exits
				PostgreSQLContainer postgresql = new PostgreSQLContainer(POSTGRESQL_IMAGE);
				postgresql.start();
				url = postgresql.getJdbcUrl();
				username = postgresql.getUsername();
				password = postgresql.getPassword();
				plugin = "Postgresql";
			}
			default -> throw new IllegalArgumentException("Unknown plugintest.database: " + DATABASE);
			}
			// What PluginTest's Properties set
			ERXProperties.setStringForKey("true", "er.extensions.ERXEC.safeLocking");
			ERXProperties.setStringForKey("false", "er.extensions.ERXEC.useSharedEditingContext");
			ERXProperties.setStringForKey("true", "er.extensions.ERXEnterpriseObject.applyRestrictingQualifierOnInsert");
			ERXProperties.setStringForKey("true", "er.extensions.ERXEnterpriseObject.updateInverseRelationships");
			ERXProperties.setStringForKey("er.extensions.jdbc.ERXJDBCAdaptor", "er.extensions.ERXJDBCAdaptor.className");
			ERXProperties.setStringForKey("true", "er.extensions.ERXJDBCAdaptor.useConnectionBroker");
			ERXProperties.setStringForKey("1", "dbMinConnectionsGLOBAL");
			ERXProperties.setStringForKey("5", "dbMaxConnectionsGLOBAL");
			ERXProperties.setStringForKey(url, MODEL_NAME + ".URL");
			ERXProperties.setStringForKey(username, MODEL_NAME + ".DBUser");
			ERXProperties.setStringForKey(password, MODEL_NAME + ".DBPassword");
			ERXProperties.setStringForKey(plugin, MODEL_NAME + ".DBPlugin");
			ERXProperties.setStringForKey("EOJDBC" + plugin + "Prototypes", MODEL_NAME + ".EOPrototypesEntity");
			// What an app does at launch
			ERXConfigurationManager.defaultManager().setCommandLineArguments(new String[0]);
			ERXFrameworkPrincipal.setUpFrameworkPrincipalClass(ERXExtensions.class);
			ERXFrameworkPrincipal.sharedInstance(ERXExtensions.class).bundleDidLoad(null);
			EOModelGroup.defaultGroup().addModelWithPathURL(PluginTestCase.class.getResource("/" + MODEL_NAME + ".eomodeld"));
			_isSetUp = true;
		}
	}

	/**
	 * Drops the model's tables, if they exist, and creates them.
	 *
	 * @param model the model
	 * @param databaseName the database's name for {@link ERXSQLHelper#newSQLHelper(String)}
	 */
	protected static synchronized void createSchema(EOModel model, String databaseName) {
		ERXSQLHelper helper = ERXSQLHelper.newSQLHelper(databaseName);
		try {
			executeScript(model, helper.createSchemaSQLForEntitiesInModelAndOptions(model.entities(), model, helper.defaultOptionDictionary(false, true)));
		}
		catch (RuntimeException e) {
			// There's nothing to drop yet. That fails in a transaction of its own, because
			// after an error PostgreSQL refuses the rest of the transaction.
			log.info("drop failure");
		}
		executeScript(model, helper.createSchemaSQLForEntitiesInModelAndOptions(model.entities(), model, helper.defaultOptionDictionary(true, false)));
		_hasSchema = true;
	}

	/**
	 * Runs a SQL script in a transaction.
	 */
	private static void executeScript(EOModel model, String sql) {
		log.debug(sql);
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		try {
			ERXEOAccessUtilities.ChannelAction action = new ERXEOAccessUtilities.ChannelAction() {
				@Override
				protected int doPerform(EOAdaptorChannel channel) {
					try {
						return ERXJDBCUtilities.executeUpdateScript(channel, sql);
					}
					catch (SQLException e) {
						throw new NSForwardException(e);
					}
				}
			};
			action.perform(ec, model.name());
		}
		finally {
			ec.dispose();
		}
	}

	/**
	 * Has {@code ERXEC} hand out a new editing context each time, and keep track of it.
	 */
	@BeforeEach
	public void setUpPluginTest() {
		editingContexts.install();
		model = EOModelGroup.defaultGroup().modelNamed(MODEL_NAME);
	}

	/**
	 * Disposes of the test's editing contexts, and puts back {@code ERXEC}'s default factory.
	 */
	@AfterEach
	public void tearDownPluginTest() {
		editingContexts.disposeAll();
		ERXEC.setFactory(null);
	}

	/**
	 * Replaces the rows in all the tables with the data in {@code world.sql}, and makes EOF
	 * forget what it had fetched before. Creates the tables first, if need be.
	 */
	protected void resetData() {
		synchronized (PluginTestCase.class) {
			if (!_hasSchema) {
				createSchema(model, adaptorName);
			}
		}
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ERXEOAccessUtilities.ChannelAction action = new ERXEOAccessUtilities.ChannelAction() {
			@Override
			protected int doPerform(EOAdaptorChannel channel) {
				try (InputStream in = PluginTestCase.class.getResourceAsStream("/world.sql")) {
					ERXJDBCUtilities.executeUpdateScript(channel, "update city set countryID = null;\n" +
							"update country set capitalID = null;\n" +
							"update countrylanguage set countryID = null;\n" +
							"delete from city;\n " +
							"delete from countrylanguage;\n" +
							"delete from country;");
					ERXJDBCUtilities.executeUpdateScript(channel, new String(in.readAllBytes(), StandardCharsets.UTF_8));
				}
				catch (SQLException | IOException e) {
					throw new NSForwardException(e);
				}
				return 0;
			}
		};
		action.perform(ec, model.name());
		// The rows changed behind EOF's back
		EOObjectStoreCoordinator.defaultCoordinator().invalidateAllObjects();
	}
}
