package todo.record.configuration;

/**
 * Interface to describe an instance of a configuration represented as a record.
 * The implementing class must be a record and have an integer field "version"
 * that is incremented for each new version.
 * 
 * When the configuration scheme is updated, create a new record with the new
 * scheme, increment its version, override transformToNextVersion() for the
 * previous record and add the new record in the list when instantiating
 * RecordConfigurationHandler.
 */
public interface RecordConfigurationSchema {
	/**
	 * The version uniquely identifies the fields and their hierarchy of the record
	 * that implements this interface. The first version must be 1 and then always +
	 * 1 for the next version.
	 * 
	 * @return Version of this record configuration where a higher number represents
	 *         a later version.
	 */
	int getVersion();

	/**
	 * Transforms an instance of this scheme to the next scheme. By default throws
	 * an UnsupportedOperationException as this must be implemented when a new
	 * scheme is developed
	 * 
	 * @return Migrated instance where getVersion() returns this.getVersion() + 1
	 * @throws UnsupportedOperationException If this.getVersion() == 1
	 */
	default RecordConfigurationSchema transformToNextVersion() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
