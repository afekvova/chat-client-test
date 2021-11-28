package ua.lil.chat.helpers;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogHelper {

    public static final Charset UNICODE_CHARSET = StandardCharsets.UTF_8;
    public static final Charset ASCII_CHARSET = StandardCharsets.US_ASCII;

    public static final String CROSS_SEPARATOR = "/";
    public static final FileSystem FS = FileSystems.getDefault();
    public static final String PLATFORM_SEPARATOR = FS.getSeparator();

    public static final String DEBUG_PROPERTY = "chat-test.debug";
    public static final String NO_JANSI_PROPERTY = "chat-test.noJAnsi";
    public static final boolean JANSI;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.US);
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(Boolean.getBoolean(DEBUG_PROPERTY));
    private static final Set<Output> OUTPUTS = Collections.newSetFromMap(new ConcurrentHashMap<>(2));
    private static final Output STD_OUTPUT;

    private static final OpenOption[] READ_OPTIONS = {StandardOpenOption.READ};
    private static final OpenOption[] WRITE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
    private static final OpenOption[] APPEND_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};

    private static final LinkOption[] LINK_OPTIONS = {};
    private static final CopyOption[] COPY_OPTIONS = {StandardCopyOption.REPLACE_EXISTING};
    private static final Set<FileVisitOption> WALK_OPTIONS = Collections.singleton(FileVisitOption.FOLLOW_LINKS);

    private static final Pattern CROSS_SEPARATOR_PATTERN = Pattern.compile(CROSS_SEPARATOR, Pattern.LITERAL);
    private static final Pattern PLATFORM_SEPARATOR_PATTERN = Pattern.compile(PLATFORM_SEPARATOR, Pattern.LITERAL);

    static {
        boolean jansi;
        try {
            if (Boolean.getBoolean(NO_JANSI_PROPERTY)) {
                jansi = false;
            } else {
                Class.forName("org.fusesource.jansi.Ansi");
                AnsiConsole.systemInstall();
                jansi = true;
            }
        } catch (ClassNotFoundException ignored) {
            jansi = false;
        }

        JANSI = jansi;

        STD_OUTPUT = System.out::println;
        addOutput(STD_OUTPUT);

        String logFile = System.getProperty("launcher.logFile");
        if (logFile != null) {
            try {
                addOutput(toPath(logFile));
            } catch (Exception e) {
                error(e);
            }
        }
    }

    public static Path toPath(String path) {
        return Paths.get(CROSS_SEPARATOR_PATTERN.matcher(path).replaceAll(Matcher.quoteReplacement(PLATFORM_SEPARATOR)));
    }

    public static void addOutput(Output output) {
        OUTPUTS.add(Objects.requireNonNull(output, "output"));
    }

    public static void addOutput(Path file) throws Exception {
        if (JANSI) {
            addOutput(new JAnsiOutput(newOutput(file, true)));
        } else {
            addOutput(newWriter(file, true));
        }
    }

    public static BufferedWriter newWriter(OutputStream output) {
        return new BufferedWriter(new OutputStreamWriter(output, UNICODE_CHARSET));
    }

    public static BufferedWriter newWriter(Path file) throws IOException {
        return newWriter(file, false);
    }

    public static BufferedWriter newWriter(Path file, boolean append) throws IOException {
        createParentDirs(file);
        return Files.newBufferedWriter(file, UNICODE_CHARSET, append ? APPEND_OPTIONS : WRITE_OPTIONS);
    }

    public static BufferedWriter newWriter(FileDescriptor fd) {
        return newWriter(new FileOutputStream(fd));
    }

    public static OutputStream newOutput(Path file) throws IOException {
        return newOutput(file, false);
    }

    public static OutputStream newOutput(Path file, boolean append) throws IOException {
        createParentDirs(file);
        return Files.newOutputStream(file, append ? APPEND_OPTIONS : WRITE_OPTIONS);
    }

    public static void addOutput(Writer writer) throws IOException {
        addOutput(new WriterOutput(writer));
    }

    public static void debug(String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, false);
        }
    }

    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    public static void error(Throwable exc) {
        error(isDebugEnabled() ? toString(exc) : exc.toString());
    }

    public static void error(String message) {
        log(Level.ERROR, message, false);
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    public static void info(String message) {
        log(Level.INFO, message, false);
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED.get();
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        DEBUG_ENABLED.set(debugEnabled);
    }

    public static void log(Level level, String message, boolean sub) {
        String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        println(JANSI ? ansiFormatLog(level, dateTime, message, sub) :
                formatLog(level, message, dateTime, sub));
    }

    public static synchronized void println(String message) {
        for (Output output : OUTPUTS) {
            output.println(message);
        }
    }

    public static boolean removeOutput(Output output) {
        return OUTPUTS.remove(output);
    }

    public static boolean removeStdOutput() {
        return removeOutput(STD_OUTPUT);
    }

    public static void subDebug(String message) {
        if (isDebugEnabled())
            log(Level.DEBUG, message, true);
    }

    public static void subDebug(String format, Object... args) {
        subDebug(String.format(format, args));
    }

    public static void subInfo(String message) {
        log(Level.INFO, message, true);
    }

    public static void subInfo(String format, Object... args) {
        subInfo(String.format(format, args));
    }

    public static void subWarning(String message) {
        log(Level.WARNING, message, true);
    }

    public static void subWarning(String format, Object... args) {
        subWarning(String.format(format, args));
    }

    public static String toString(Throwable exc) {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                exc.printStackTrace(pw);
            }
            return sw.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static void warning(String message) {
        log(Level.WARNING, message, false);
    }

    public static void warning(String format, Object... args) {
        warning(String.format(format, args));
    }

    public static void createParentDirs(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !isDir(parent))
            Files.createDirectories(parent);
    }

    public static boolean isDir(Path path) {
        return Files.isDirectory(path, LINK_OPTIONS);
    }

    private static String ansiFormatLog(Level level, String dateTime, String message, boolean sub) {
        Color levelColor;
        boolean bright = level != Level.DEBUG;
        switch (level) {
            case WARNING:
                levelColor = Color.YELLOW;
                break;
            case ERROR:
                levelColor = Color.RED;
                break;
            default: // INFO, DEBUG, Unknown
                levelColor = Color.WHITE;
                break;
        }

        Ansi ansi = new Ansi();
        ansi.fg(Color.WHITE).a(dateTime);

        ansi.fgBright(Color.WHITE).a(" [").bold();
        if (bright) {
            ansi.fgBright(levelColor);
        } else {
            ansi.fg(levelColor);
        }
        ansi.a(level).boldOff().fgBright(Color.WHITE).a("] ");

        if (bright) {
            ansi.fgBright(levelColor);
        } else {
            ansi.fg(levelColor);
        }
        if (sub) {
            ansi.a(' ').a(Attribute.ITALIC);
        }
        ansi.a(message);

        return ansi.reset().toString();
    }

    private static String formatLog(Level level, String message, String dateTime, boolean sub) {
        if (sub) {
            message = ' ' + message;
        }
        return dateTime + " [" + level.name + "] " + message;
    }

    public enum Level {
        DEBUG("DEBUG"), INFO("INFO"), WARNING("WARN"), ERROR("ERROR");
        public final String name;

        Level(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    public interface Output {
        void println(String message);
    }

    private static final class JAnsiOutput extends WriterOutput {
        private JAnsiOutput(OutputStream output) throws IOException {
            super(newWriter(new AnsiOutputStream(output)));
        }
    }

    private static class WriterOutput implements Output, AutoCloseable {
        private final Writer writer;

        private WriterOutput(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        @Override
        public void println(String message) {
            try {
                writer.write(message + System.lineSeparator());
                writer.flush();
            } catch (IOException ignored) {
                // Do nothing?
            }
        }
    }
}
