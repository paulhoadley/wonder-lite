ERJavaMail
==========
Sends mail from WebObjects applications using the JavaMail API. Mail
is sent on a separate thread, inside the application's own JVM.

ERJavaMail began as the Odaiko MailDelivery framework by Camille
Troillard.

Licence
-------
The original framework's README carried this notice, kept here
because the source files have no licence notice of their own:

```
These files are protected under the GNU Lesser Public License.
You should read and understand the terms of this license before using it.
The article 15 in the LICENSE read this:

	  15. BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
	WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE
	LAW.  EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS
	AND/OR OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY 
	OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT 
	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
	FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS TO THE QUALITY AND 
	PERFORMANCE OF THE LIBRARY IS WITH YOU.  SHOULD THE LIBRARY PROVE 
	DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR 
	OR CORRECTION.
```

Configuration
-------------
* `er.javamail.centralize`: send every message to
  `er.javamail.adminEmail` instead of its recipients.
* `er.javamail.adminEmail`: where centralised mail goes. Required.
* `er.javamail.debugEnabled`: show protocol-level debugging output.
* `er.javamail.smtpHost`: the SMTP host. If it isn't set,
  `mail.smtp.host` is used, then `WOHost`.
* `er.javamail.smtpPort`: the SMTP port. Defaults to 25.
* `er.javamail.smtpProtocol`: `smtp` or `smtps`. Rarely needed.
* `er.javamail.smtpAuth`: use authenticated SMTP. If it's `true`,
  also set `er.javamail.smtpUser` and `er.javamail.smtpPassword`.
* `er.javamail.senderQueue.size`: how many messages the sender queue
  holds. Defaults to 50.
* `er.javamail.milliSecondsWaitIfSenderOverflowed`: how long to wait
  when the sender queue is full. Defaults to 6000.
* `er.javamail.XMailerHeader`: an `X-Mailer` header for every message.
  None by default.
* `er.javamail.defaultEncoding`: the default character encoding for
  message content.
* `er.javamail.emailPattern`: the regular expression used to validate
  addresses. A built-in pattern is used if it isn't set.
* `er.javamail.AllowEmailAddressPatterns`: if set, mail is only
  delivered to addresses matching one of these patterns.
* `er.javamail.DenyEmailAddressPatterns`: mail is never delivered to
  addresses matching one of these patterns. Deny patterns are applied
  last, so they win over allow patterns.

Address patterns use `EOQualifier`'s `caseInsensitiveLike` syntax.
Write the list as a property list array, quoting each pattern:

```
er.javamail.AllowEmailAddressPatterns=("*@example.com", "someone@example.org")
```

Less common settings, such as configuring the mail session through
JNDI, are read in `ERJavaMail.java`.

Example
-------
```java
// Create an instance of an ERMailDelivery subclass. ERMailDeliveryHTML
// renders its HTML content from a WOComponent.
ERMailDeliveryHTML mail = new ERMailDeliveryHTML();
mail.setComponent(mailPage);

// Call newMail() before setting each message's attributes. You can
// reuse one instance to send several messages.
try {
	mail.newMail();
	mail.setFromAddress(emailFrom);
	mail.setReplyToAddress(emailReplyTo);
	mail.setSubject(emailSubject);
	mail.setToAddresses(new NSArray<String>(toEmailAddresses));
	// sendMail(boolean) can block until the message is sent.
	mail.sendMail();
} catch (Exception e) {
	// Handle the exception.
}
```

Gotchas
-------
Be careful with the `WOContext` of the component you send. If you use
`ERMailDeliveryHTML` during the normal request-response loop with the
default context, the next page the user sees is likely to be the
emailed component rather than the page you meant to return. Rendering
the email in a clone of the current context avoids this:

```java
WOContext context = (WOContext) context().clone();
MyComponent component = (MyComponent) WOApplication.application().pageWithName(MyComponent.class.getName(), context);
ERMailDeliveryHTML mail = new ERMailDeliveryHTML();
mail.setComponent(component);
```
