# vertx-http-server-groovy

This repo is to reproduce the following issue:
run:
1) mvn install
2) Observe the following output: 
Exception in thread "Thread-2" java.lang.NoClassDefFoundError: io/vertx/core/file/FileSystemException
	at io.vertx.core.file.impl.FileSystemImpl.deleteInternal(FileSystemImpl.java:585)
	at io.vertx.core.file.impl.FileSystemImpl.deleteRecursive(FileSystemImpl.java:212)
	at io.vertx.core.impl.FileResolver.deleteCacheDir(FileResolver.java:315)
	at io.vertx.core.impl.FileResolver.lambda$setupCacheDir$1(FileResolver.java:304)
	at java.lang.Thread.run(Thread.java:745)
Caused by: java.lang.ClassNotFoundException: io.vertx.core.file.FileSystemException
	at org.codehaus.plexus.classworlds.strategy.SelfFirstStrategy.loadClass(SelfFirstStrategy.java:50)
	at org.codehaus.plexus.classworlds.realm.ClassRealm.unsynchronizedLoadClass(ClassRealm.java:271)
	at org.codehaus.plexus.classworlds.realm.ClassRealm.loadClass(ClassRealm.java:247)
	at org.codehaus.plexus.classworlds.realm.ClassRealm.loadClass(ClassRealm.java:239)
	... 5 more


