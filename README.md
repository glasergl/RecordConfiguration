# Record Configuration
Imagine you specify a full configuration for anything using Java records.
Gson by Google already conveniently allows serializing and deserializing record instances as Json.
However, when the scheme of the configuration changes, Gson cannot be aware of that and thus, serialization as well as deserializing fails.

Now, this library comes into place.
For example, assume you have a configuration like this
```java
public record Configuration(String id, int number, OtherRecord other) {
}
```
and now you would need to modify the record in any way, e.g., add another field `name`
```java
public record Configuration2(String id, int number, OtherRecord other, String name) {
}
```

First, implement the `RecordConfigurationSchema` interface like this for both configuration records
```java
public record Configuration(int version, String id, int number, OtherRecord other) implements RecordConfigurationSchema {
	@Override
	public int getVersion() {
		return 1;
	}
	
	@Override
	public RecordConfigurationSchema transformToNextVersion() {
		return new Configuration2(2, this.id, this.number, this.other, "Any reasonable name");
	}
}

public record Configuration2(int version, String id, int number, OtherRecord other, String name) implements RecordConfigurationSchema {
	@Override
	public int getVersion() {
		return 2;
	}
}
```

Then you can create a handler like this
```java
final RecordConfigurationHandler handler = new RecordConfigurationHandler("config.json", Configuration.class, Configuration2.class);
Configuration2 configuration = (Configuration2) handler.get();
```
Independent whether the serialized file at "config.json" conforms to the schema of version 1 or version 2, the resulting object will have type Configuration2.

You can create any number of configuration records and define the migration from one to the next and the handler will perform the actual transformation by calling the transform method iteratively until the latest version is reached.
Obviously, implementing the `transformToNextVersion` method on the latest version is not reasonable, thus, it throws an `UnsupportedOperationException` by default.