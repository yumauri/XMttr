# XMttr HTTL

*Generator* module for XMttr, uses [HTTL](https://httl.github.io/) template engine to create messages.

#### Template example

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Message>
  <!--#var(UUID MessageId)-->
  <MessageId>${MessageId}</MessageId>
  <MessageType>${MessageType}</MessageType>
  <MessageBody>
    <Account>
      <Id>${Id}</Id>
      <Name>${Name}</Name>
      <!--#if(ShortName)-->
      <ShortName>${ShortName}</ShortName>
      <!--#end-->
      <!--#var(List<Map<String,String>> Phones)-->
      <!--#if(Phones && Phones.size)-->
      <ListOfPhone>
        <!--#for(Map<String,String> p : Phones)-->
        <Phone>
          <Phone>${p.Phone}</Phone>
          <Type>${p.Type}</Type>
        </Phone>
        <!--#end-->
      </ListOfPhone>
      <!--#end-->
    </Account>
  </MessageBody>
</Message>
```

You can see variables, declarations of variable type, cycle iteration. For detailed help see [HTTL documentation](http://httl.github.io/en/syntax.html)

#### Properties

You can use any [HTTL property](http://httl.github.io/en/config.html). Some useful parameters are here:

| property | description | default value |
|----------|-------------|---------------|
|default.variable.type|default variable type|java.lang.Object|
|filters|filters for message, e.g. *httl.spi.filters.ClearBlankFilter* – removes all linebreaks and spaces.| |
|date.format|date format, e.g. *MM/dd/yyyy HH:mm:ss*| |
