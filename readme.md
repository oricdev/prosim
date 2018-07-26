# Welcome to the Prosim project

Click [here](https://oricdev.github.io/prosim/docs) to access a detailed documentation on the project (still in progress).

## Accessing live-running Prosim-db
Access live-running Prosim-db in reader mode using these info and credentials:


> host: mongodb-tuttifrutti.alwaysdata.net

> database: tuttifrutti_off_match

> login: tuttifrutti_reader

> password: reader

When using a connection string with the code instruction new MongoClient(), it may look like the following:

> new MongoClient(new MongoClientURI("mongodb://tuttifrutti_reader:reader@mongodb-tuttifrutti.alwaysdata.net/tuttifrutti_off_match"));

Note: when running the intersect project below, you must have Write access of course in your own Prosim-db!

## Prosim is made of 3 projects
### /tasks/feeders
* feeder_1.py: extracts from MongoDb all products (used as Width matrix for the intersections) and creates
> /outputs/feeders/all_products.json

> /outputs/feeders/feeder_1.txt

* feeder_2.py: in current version (first initialisation of Prosim-db), does the same as feeder_1 (used as Width matrix for the intersections).
Shall be enhanced versions so that it extracts only new and updated products from a specific date.
> /outputs/feeders/updated_products.json

> /outputs/feeders/feeder_2.txt

Only some fields are extracted( projection), not all of them. Beware that **all_products.json** may be something like **2GB large**!

Update the code if you wish to extract more information for building your own Prosim-db.

*feeder_1.txt* is used as a **lock only**. This means that after the extraction, feeder_1 creates this empty file to tell 
himself the extraction is finished. As long as this file exists, no further extraction will be performed.

Feeders **should be scheduled** in the future **but are actually not** since *feeder_2* needs to be enhanced by taking into account new and updated products from official OFF-db from the date of last extraction (*todo*).

### /tasks/preparer
Note: check /tasks/preparer/preparer_netbeans/config.xml file

**VERY IMPORTANT**: the progression of the generation of sub-matrixes by the preparer task is saved in */tasks/preparer/preparer/netbeans/progress.xml*.

 The preparer job **stores the barcodes of the last intersected products** in there!! For a **new db**, there should not be any product code and you should have this:
 
> \<last_intersect_code_all_products\>\</last_intersect_code_all_products\>
> \<last_intersect_code_to_be_inserted\>\</last_intersect_code_to_be_inserted\>
  
Reset this file with empty tags if you wish to restart the import process from scratch (don't forget to drop your Prosim-db as well!).

The *preparer* task deletes all lock-files when feeder_1 and feeder_2 extractions' are all processed/sliced
 up in data-packages (*/outputs/preparer*) in order to enable further extractions (from feeders tasks).
 Based on the huge matrix **\[all_products.json; updated_products.json\]**, it creates 
 > /tasks/preparer/preparer_netbeans/config.xml::max_output_data sub-matrixes of size \[width ; height\]
 
and stores them into UUID_directories in */outputs/preparer/\<UUID\>/h_products.json* and *w_products.json*

When all sub-matrixes are created through all_products and updated_products Json files, it deletes all lock-files from feeders to unlock the feeding process.

This tasks should be scheduled (see crontab below).

### /tasks/intersect
Note: check /tasks/intersect/config.xml file and update the data related to the location of the Prosim-db (will be created if needed).

The intersecter processes sub_matrixes data from one UUID directory after another and backups them once finished.
It performs the following tasks:

- intersect each product of the Width-matrix with each product of the Height matrix (computation of nutrition score
 and similarity percentage). We get a list of ProductExt (extended product)
 
> while (dirDataset != null && sizeInGB < max_dbSize) {

>>   Tuple<File, File> dataset = Main.getNextDataPackage(dirDataset);

>>   List<IProduct> productsExt = intersectMatrixProducts(dataset);

>>   feedDb(productsExt);

**feedDb** is the method which does it all with Mapreduce processes:
> Mapreduces all intersections of products (productExt list): similarity properties for 2 products with same barcode are merged into a single Prosim record

> after reduction, checks for the existence or not in the Prosim-db of each reduced Prosim record: if not found, inserts it, otherwise, performs a new Mapreduction between the entry in the db and the mapreduced-one and replaces it in the db (**heavy process**!)
 
*Note*: the process makes big use of the great **Morphia opensource-project** which maps fingers-in-the-nose a MongoDb record with its object and vice-versa!
              
              
This tasks should be scheduled (see crontab below).


## Follow these steps to start with the Prosim project

Update in *tasks/preparer/preparer_netbeans/src/main/resources/log4j.properties* the entry *log4j.eppender.file.File* with appropriate path for the log's outputs:

> log4j.appender.file.File=~/prosim/project/logs/log_preparer.log

Update in *tasks/intersect/src/main/resources/log4j.properties* the entry *log4j.eppender.file.File* with appropriate path for the log's outputs:

> log4j.appender.file.File=~/prosim/project/logs/log_intersect.log

Update the following tags in *global_config.xml* for accessing your official OFF database 
(Mongo server should be started):
> <off_source_host>127.0.0.1</off_source_host>

> <off_source_port>27017</off_source_port>

> <off_source_db_name>off</off_source_db_name>

Launch feeder_1.py:
> cd prosim/project/tasks/feeders/feeder_1

> ./venv/bin/python ./feeder_1.py

This produces some log and a file outputs/feeders/all_products.json of about 2GB large.

No need to launch feeder_2 (not completed) --> copy all_products.json into /outputs/feeders/updated_products.json. Copy also feeder_1.txt into feeder_2.txt (lockers, stops feeding process).

_Note_: in the current state of the project, you will not need to run feeders anymore (first intiialisation of Prosim-db).

In Netbeans, open the preparer and intersect projects

_Note_: unfortunately for now, both logs are bound with the same logger (*todo*: to be changed so that each process owns its own log-file).

Update tasks/preparer/preparer_netbeans/config.xml file

Update tasks/intersect/config.xml file with the destination of youy **NEW** Prosim-db

Build the preparer project: Maven is configured to embed all library jar files into target/preparer-1.0-SNAPSHOT-jar-with-dependencies.jar

Build the intersect project: Maven is configured to embed all library jar files into target/preparer-1.0-SNAPSHOT-jar-with-dependencies.jar

Under the /prosim directory, update in *start_preparer* and *start_intersect* files the path to access the JVM Java 8 version:

> /usr/lib/jvm/java-8-openjdk-amd64/bin/java

Make these 2 files executable:
> chmod 774 start_preparer

> chmod 774 start_intersect

To check everything goes fine: start the start_preparer job:

> ./start_preparer

It should produce a console log and create several pieces of outputs/\<UUID\> directories

When it is finished, start the intersecter

> ./start_intersect

It should create the **empty Prosim-db** and exports your first records!

Congratulations, you've achieved it!

**You can now schedule these 2 tasks for running twice per hour:**
> crontab -e

Add the end of the file the following lines and saves it:

> \# preparer

> 0,30 * * * * prosim/start_preparer

> \# intersecter

> 15,45 * * * * prosim/start_intersecter

> \# clean backup directory each hour

> 59 * * * * rm -r prosim/project/backup/*

*Note*: a regular cleanup is required if you lack from disk space (without any deletion, you may get hundreds of GB of data!).

Feel free to contact the author in case of questions, remarks, suggestions. I will be very happy to help :).
