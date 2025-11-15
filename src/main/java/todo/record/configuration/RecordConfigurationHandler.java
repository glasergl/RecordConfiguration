package todo.record.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Handler object to get and update configuration files. The class is aware of
 * all ever existing configuration schemes and is able to migrate from any to
 * the latest.
 * 
 * The file accesses are not thread safe.
 */
public final class RecordConfigurationHandler {
	private final List<Class<? extends RecordConfigurationSchema>> configurationSchemas;
	private final Path configurationFilePath;
	private final Gson jsonParser = new Gson();

	/**
	 * @param configurationSchemas  - all currently implemented configuration
	 *                              schemes, implicitly it is assumed that the given
	 *                              classes represent the schema version from 1 to n
	 *                              where n is the length of the list
	 * @param configurationFilePath where this should act as a handler for
	 */
	public RecordConfigurationHandler(final List<Class<? extends RecordConfigurationSchema>> configurationSchemas,
			final String configurationFilePath) {
		super();
		if (!configurationSchemas.stream().allMatch(Class::isRecord)) {
			throw new IllegalArgumentException("all classes must be records");
		}
		this.configurationSchemas = configurationSchemas;
		this.configurationFilePath = Path.of(configurationFilePath);
	}

	/**
	 * Deserializes the configuration file and updates, if necessary.
	 * 
	 * @return Configuration conforming to the latest schema, empty if
	 *         deserialization or updating failed
	 */
	public Optional<? extends RecordConfigurationSchema> get() {
		try {
			final String fileContent = Files.readString(configurationFilePath, StandardCharsets.UTF_8);
			final int currentVersion = getVersion(fileContent);
			final int latestVersion = configurationSchemas.size();
			final RecordConfigurationSchema deserializedCurrentVersion = jsonParser.fromJson(fileContent,
					configurationSchemas.get(currentVersion - 1));
			RecordConfigurationSchema deserializedLatestVersion = deserializedCurrentVersion;
			for (int i = currentVersion + 1; i <= latestVersion; i++) {
				deserializedLatestVersion = deserializedLatestVersion.transformToNextVersion();
			}
			return Optional.of(deserializedLatestVersion);
		} catch (final IOException | RuntimeException e) {
			return Optional.empty();
		}
	}

	/**
	 * @param configuration which should be serialized
	 * @return Whether serialization was done successfully
	 */
	public boolean write(final RecordConfigurationSchema configuration) {
		try {
			final String json = jsonParser.toJson(configuration);
			Files.write(configurationFilePath, json.getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (final IOException | RuntimeException e) {
			return false;
		}
	}

	/**
	 * Reads the version field from the given json content.
	 * 
	 * @param configurationContent - valid json with a int value at the field
	 *                             version
	 * @return Integer version number at the top level of the given json content
	 * @throws IOException Any IOException is forwared, no handling here
	 */
	private int getVersion(final String configurationContent) throws IOException {
		final JSONObject configurationJson = new JSONObject(configurationContent);
		if (!configurationJson.has("version")) {
			throw new IllegalArgumentException("Configuration doesn't have a version");
		}
		return configurationJson.getInt("version");
	}
}
