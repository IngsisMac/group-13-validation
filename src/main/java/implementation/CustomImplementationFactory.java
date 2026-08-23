package implementation;

import com.printscript.common.EnvSource;
import com.printscript.common.InputSource;
import com.printscript.common.OutputEmitter;
import com.printscript.common.PrintScriptError;
import com.printscript.common.Version;
import com.printscript.runner.ExecutionResult;
import com.printscript.runner.PrintScriptRunner;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CustomImplementationFactory implements PrintScriptFactory {

    @Override
    public PrintScriptInterpreter interpreter() {
        return (src, versionStr, emitter, handler, provider) -> {
            try {
                Version version = resolveVersion(versionStr);
                if (version == null) {
                    handler.reportError("Unknown version: " + versionStr);
                    return;
                }
                try (Reader reader = new BufferedReader(new InputStreamReader(src, StandardCharsets.UTF_8), 768 * 1024)) {
                    OutputEmitter outputEmitter = emitter::print;
                    InputSource inputSource = provider::input;
                    EnvSource envSource = System::getenv;

                    ExecutionResult result = PrintScriptRunner.INSTANCE.execute(
                        reader,
                        version,
                        outputEmitter,
                        inputSource,
                        envSource
                    );

                    for (PrintScriptError error : result.getErrors()) {
                        handler.reportError(error.render());
                    }
                } catch (IOException e) {
                    handler.reportError("IO Error: " + e.getMessage());
                }
            } catch (OutOfMemoryError e) {
                handler.reportError("Java heap space");
            }
        };
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