package implementation;

import com.printscript.common.Version;
import com.printscript.runner.PrintScriptRunner;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CustomImplementationFactory implements PrintScriptFactory {

    @Override
    public PrintScriptInterpreter interpreter() {
        // your PrintScript implementation should be returned here.
        // make sure to ADAPT your implementation to PrintScriptInterpreter interface.
        throw new NotImplementedException("Needs implementation"); // TODO: implement
    }

    @Override
    public PrintScriptFormatter formatter() {
        return (src, versionStr, config, writer) -> {
            Version version = resolveVersion(versionStr);
            if (version == null) {
                return;
            }
            try (Reader srcReader = new InputStreamReader(src, StandardCharsets.UTF_8);
                 Reader configReader = new InputStreamReader(config, StandardCharsets.UTF_8)) {

                PrintScriptRunner.INSTANCE.format(
                    srcReader,
                    version,
                    configReader,
                    writer
                );
            } catch (IOException e) {
                // Ignore IO errors in formatter stream close
            }
        };
    }

    @Override
    public PrintScriptLinter linter() {
        return (src, versionStr, config, handler) -> {
            try {
                Version version = resolveVersion(versionStr);
                if (version == null) {
                    handler.reportError("Unknown version: " + versionStr);
                    return;
                }
                try (Reader srcReader = new InputStreamReader(src, StandardCharsets.UTF_8);
                     Reader configReader = new InputStreamReader(config, StandardCharsets.UTF_8)) {

                    PrintScriptRunner.INSTANCE.analyze(
                        srcReader,
                        version,
                        configReader,
                        error -> {
                            handler.reportError(error.render());
                            return kotlin.Unit.INSTANCE;
                        }
                    );
                } catch (IOException e) {
                    handler.reportError("IO Error: " + e.getMessage());
                }
            } catch (OutOfMemoryError e) {
                handler.reportError("Java heap space");
            }
        };
    }

    private static Version resolveVersion(String versionStr) {
        for (Version v : Version.values()) {
            if (v.getIdentifier().equals(versionStr)) {
                return v;
            }
        }
        return null;
    }
}