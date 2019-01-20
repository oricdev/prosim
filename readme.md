# Welcome to the Prosim project

***PROSIM*** stands for PROduct SIMilarity.

It is the backend-engine empowering its client-side pendant application ***off_graph*** which is accessible on the [Tuttifrutti](https://tuttifrutti.alwaysdata.net/) website.

Basically, it computes pair-wise the *similarity* and *score* between **OpenFoodFacts** products. Scores can be of different kinds: *Nutrition-score*, *Nova Classification*, or even [your own score](https://offmatch.blogspot.com/2018/10/how-can-i-request-new-score-database.html).

More resources here:
- PROSIM blog:\
https://offmatch.blogspot.com/
- **architecture** of PROSIM explained:\
https://offmatch.blogspot.com/2018/10/architecture-map.html
- OFF-GRAPH blog (client):\
https://offgraphs.blogspot.com/2018/09/api-usage-for-getting-similar-products.html
- the TUTTIFRUTTI website (top of the iceberg):\
https://tuttifrutti.alwaysdata.net/
- TWITTER:\
https://twitter.com/GraphProsim

Extra resources:
- OpenFoodFacts [Fr](https://fr.openfoodfacts.org/) | [En](https://world.openfoodfacts.org/)
- OpenFoodFacts on *Twitter* [Fr](https://twitter.com/OpenFoodFactsFr) | [En](https://twitter.com/OpenFoodFacts)
- Nutriscore [Fr](https://fr.openfoodfacts.org/nutriscore) | [En](https://world.openfoodfacts.org/nutriscore)
- Nova Classification [Fr](https://quoidansmonassiette.fr/aliments-ultra-transformes-nova-classification-ultraprocessed-nouvelle-approche-nutrition-sante-publique/)
| [En](https://www.bmj.com/content/bmj/360/bmj.k322.full.pdf)


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
* feeder_1: extracts from MongoDb all products (used as Width matrix for the intersections) and creates
> /outputs/feeders/all_products.json

> /outputs/feeders/feeder_1.txt

* feeder_2: in current version (first initialisation of Prosim-db), does the same as feeder_1 (used as Heigth matrix for the intersections).
Could be enhanced in future versions so that it extracts only new and updated products from a specific date.
> /outputs/feeders/updated_products.json

> /outputs/feeders/feeder_2.txt

Feeders filter **candidate** products so that an intersection and a score can be computed. So only a subset of all products from OpenFoodFacts are being extracted.

Furthermore, only some fields are extracted( projection), not all of them. Beware that **all_products.json** may be something like **1GB or 2GB large**!

Update the code if you wish to extract more information for building your own Prosim-db.

*feeder_1.txt* is used as a **lock only**. This means that after the extraction, feeder_1 creates this empty file to tell 
himself the extraction is finished. As long as this file exists, no further extraction will be performed.

Feeders **should be scheduled** in the future **but are actually not** since they are launched manually for ease of use first, and also because *feeder_2* needs to be enhanced by taking into account new and updated products from official OFF-db from the date of last extraction.

### /tasks/preparer
Note: check /tasks/preparer/preparer_netbeans/config.xml file

**VERY IMPORTANT**: the progression of the generation of sub-matrixes by the preparer task is saved in */tasks/preparer/preparer/netbeans/progress.xml*.

 The preparer job **stores the barcodes of the last intersected products** in there!! For a **new db**, there should not be any product code and you should have this:
 
> \<last_intersect_code_all_products\>\</last_intersect_code_all_products\>
> \<last_intersect_code_to_be_inserted\>\</last_intersect_code_to_be_inserted\>
  
Reset this file with empty tags if you wish to restart the import process from scratch (don't forget to drop your old Prosim-db as well!).

The *preparer* task deletes all lock-files when feeder_1 and feeder_2 extractions' are all processed/sliced
 up in data-packages (*/outputs/preparer*) in order to enable further extractions (from feeders tasks).
 Based on the huge matrix **\[all_products.json; updated_products.json\]**, it creates 
 > /tasks/preparer/preparer_netbeans/config.xml::max_output_data sub-matrixes of size \[width ; height\]
 
and stores them into UUID_directories in */outputs/preparer/\<UUID\>/h_products.json* and *w_products.json*

When all sub-matrixes are created through *all_products* and *updated_products* Json files, it deletes all lock-files from feeders to unlock the feeding process.

This tasks should be scheduled (see crontab below).

### /tasks/intersect
Note: check /tasks/intersect/config.xml file and update the data related to the location of the Prosim-db (the database will be created if needed).

The intersecter processes sub_matrixes data from one UUID directory after another (oldest directory first), and deletes them once finished.

In some cases, the JSON data files may contain errors: it happen sometimes when *feeders* and *preparer* run on different machines with different versions for the Json Writer libraries. So in case of errors, the UUID directory is moved to *../errors* directory and data are not lost.

The intersecter performs the following tasks:

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

Launch feeder_1:
> cd prosim/project/tasks/feeder_1/target

> java -Xmx400M -cp feeder_1-1.0-SNAPSHOT.jar org.openfoodfacts.feeders/Main

This produces some log and a file outputs/feeders/all_products.json of about 2GB large.

No need to launch feeder_2 (as long as it has not been enhanced as described above) --> copy all_products.json into /outputs/feeders/updated_products.json. Copy also feeder_1.txt into feeder_2.txt (lockers, stops feeding process).

_Note_: in the current state of the project, you will not need to run feeders anymore (first intiialisation of Prosim-db).

In Netbeans (or other Development interface), open the preparer and intersect projects

_Note_: unfortunately for now, both logs are bound with the same logger (*todo*: to be changed so that each process owns its own log-file).

Update tasks/preparer/preparer_netbeans/config.xml file

Update tasks/intersect/config.xml file with the destination of your **NEW** Prosim-db

Build the preparer project: Maven is configured to embed all library jar files into target/preparer-1.0-SNAPSHOT-jar-with-dependencies.jar

Build the intersect project: Maven is configured to embed all library jar files into target/intersect-1.0-SNAPSHOT-jar-with-dependencies.jar

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

*Note*: erroneous data files (as mentioned above) are **not deleted** but moved to *../errors*. Check this location regularly if you ever lack from disk space.

## Customizing Prosim to run on another Score
The code delivered on github under the *project* directory performs computations of similar products based on their **Nutrition-score** (Nutriscore).

This computation is fully customizable and is contained into a file called **ComputingInstance.java**.

The *Nutriscore* version of this file has been injected into the *project* after compilation:
> prosim/project/tasks/preparer/preparer_netbeans/src/main/java/org/openfoodfacts/computers/ComputingInstance.class

If you want the Prosim project to compute similarities of OpenFoodFact products using another score (e.g. *Nova 
Classification* or your own-one; check in the *ComputingInstances* directory above for other versions), you have to **COMPILE** and **INJECT**
the corresponding *ComputingInstance.class* file at this exact location (under *computers* directory).

Personally, I am doing it this way:
- update of the *ComputingInstance.java* file: it has at the end to be called this way!
- compile it with dependencies (held in the *jar* of preparer task) in order to get a *ComputingInstance.class* file:
> javac -cp project/tasks/preparer/preparer_netbeans/target/preparer_netbeans-1.0-SNAPSHOT-jar-with-dependencies.jar ComputingInstance.java
- unzip the file:
> prosim/project/tasks/intersect/target/intersect-1.0-SNAPSHOT-jar-with-dependencies.jar
- replace the *ComputingInstance.class* with the one you have just created inside the unzipped jar file:
> intersect-1.0-SNAPSHOT-jar-with-dependencies/org/openfoodfacts/computers/
- zip back as a *jar* the content of this directory:
> intersect-1.0-SNAPSHOT-jar-with-dependencies
- you can now launch the **intersecter** using this command directly or through a script:
> java -cp target/intersect-1.0-SNAPSHOT-jar-with-dependencies.jar org.openfoodfacts.intersect/Main


Feel free to contact the author in case of questions, remarks, suggestions. I will be happy to help :).
