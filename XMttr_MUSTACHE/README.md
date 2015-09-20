# XMttr MUSTACHE

*Generator* module for XMttr, uses [{{mustache}}](https://mustache.github.io) template engine to create messages.

#### Template example

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Message>
  <MessageId>{{MessageId}}</MessageId>
  <MessageType>{{MessageType}}</MessageType>
  <MessageBody>
    <Account>
      <Id>{{Id}}</Id>
      <Name>{{Name}}</Name>
      {{#ShortName}}
      <ShortName>{{ShortName}}</ShortName>
      {{/ShortName}}
      {{#Phones.size}}
      <ListOfPhone>
        {{#Phones}}
        <Phone>
          <Phone>{{Phone}}</Phone>
          <Type>{{Type}}</Type>
        </Phone>
        {{/Phones}}
      </ListOfPhone>
      {{/Phones.size}}
    </Account>
  </MessageBody>
</Message>
```

For detailed help see [{{mustache}} documentation](https://mustache.github.io/mustache.5.html)

#### Properties

| property | description | default value |
|----------|-------------|---------------|
|date.format|date format, e.g. *MM/dd/yyyy HH:mm:ss*| |
