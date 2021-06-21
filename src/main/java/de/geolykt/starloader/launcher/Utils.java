package de.geolykt.starloader.launcher;

import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.mixin.Mixins;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.extras.selfmodification.mixins.MixinCodeModifier;
import net.minestom.server.extras.selfmodification.mixins.MixinServiceMinestom;

/**
 * Collection of static utility methods.
 */
public final class Utils {

    /**
     * The constructor.
     * DO NOT CALL THE CONSTRUCTOR.
     */
    private Utils() {
        // Do not construct classes for absolutely no reason at all
        throw new RuntimeException("Didn't the javadoc tell you to NOT call the constructor of this class?");
    }

    public static final File getOneOfExistingFiles(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static final boolean isJava9() {
        try {
            URLClassLoader.getPlatformClassLoader(); // This should throw an error in Java 8 and below
            // I am well aware that this will never throw an error due to Java versions, but it's stil a bit of futureproofing
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static final void startMain(Class<?> className, String[] args) {
        try {
            Method main = className.getDeclaredMethod("main", String[].class);
            main.setAccessible(true);
            // Note to self future : do not attempt to cast the class(es), it won't work well.
            // This means that the array instantiation is intended
            // Edit: 21 Jun 2021: Damn, you read my mind
            main.invoke(null, new Object[] { args });
        } catch (Exception e) {
            throw new RuntimeException("Error while invoking main class!", e);
        }
    }

    @SuppressWarnings("resource")
    protected static final void startGalimulator(String[] args, LauncherConfiguration preferences) {
        if (isJava9()) {
            MinestomRootClassLoader cl = MinestomRootClassLoader.getInstance();
            try {
                cl.addURL(preferences.getTargetJar().toURI().toURL());
            } catch (MalformedURLException e1) {
                throw new RuntimeException("Something went wrong while adding the target jar to the Classpath", e1);
            }
            try {
                if (preferences.hasExtensionsEnabled()) {
                    startMixin(args);
                    cl.addCodeModifier(new MixinCodeModifier());
                    MixinServiceMinestom.gotoPreinitPhase();
                    // ensure extensions are loaded when starting the server
                    Class<?> serverClass = cl.loadClass("de.geolykt.starloader.Starloader");
                    Method init = serverClass.getMethod("init");
                    init.invoke(null);
                    MixinServiceMinestom.gotoInitPhase();
                    MixinServiceMinestom.gotoDefaultPhase();
                }
                File loadingJar = preferences.getTargetJar();
                String main = null;
                try (JarFile jar = new JarFile(loadingJar)) {
                    main = jar.getManifest().getMainAttributes().getValue("Main-Class");
                    // Setup log4J configuration
                    ZipEntry e = jar.getEntry("log4j2.xml");
                    if (e != null) {
                        InputStream log4jConfig = jar.getInputStream(e);
                        Configurator.initialize(cl, new ConfigurationSource(log4jConfig));
                    }
                }
                startMain(cl.loadClass(main), args);
            } catch (Exception e1) {
                throw new RuntimeException("Something went wrong while bootstrapping.", e1);
            }
        } else {
            throw new UnsupportedOperationException("Java 8 or earlier is not supported.");
        }
    }

    protected static final void startMixin(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // hacks required to pass custom arguments
        Method start = MixinBootstrap.class.getDeclaredMethod("start");
        start.setAccessible(true);
        if (!((boolean)start.invoke(null))) {
            return;
        }

        Method doInit = MixinBootstrap.class.getDeclaredMethod("doInit", CommandLineOptions.class);
        doInit.setAccessible(true);
        doInit.invoke(null, CommandLineOptions.ofArgs(Arrays.asList(args)));

        MixinBootstrap.getPlatform().inject();
        Mixins.getConfigs().forEach(c -> MinestomRootClassLoader.getInstance().protectedPackages.add(c.getConfig().getMixinPackage()));
    }

    public static final Dimension combineLargest(Dimension original, Dimension newer) {
        return new Dimension(Math.max(original.width, newer.width), Math.max(original.height, newer.height));
    }

    public static File getCurrentDir() {
        return new File(".");
    }
}
