[markdown syntax](https://help.github.com/articles/basic-writing-and-formatting-syntax/)
# Welcome to the Prosim project

Prosim stands for **PRO**duct **SIM**ilarity.

The acronym **OFF** used in this documentation stands for **O**pen**F**ood**F**acts.

## goal
This project aims at aggregating information
delivered by the [OpenFoodFacts](https://world.openfoodfacts.org) collaborative database in order to deliver more or less similar
 products to a reference product of your choice, classified for instance by their [nutrition score](), but it could be 
 enhanced by the community to take into account other criteria rgearding `vegans`, `additives`, `allergens`, etc.

## short history
### ante-Prosim: the tuttifrutti website (proof-of-concept)
I developped a first website [tuttifrutti](http://tuttifrutti.alwaysdata.net/) with the idea of getting *instantly*
**better products** and pretty similar to the one whose **barcode** I scanned through the
[OFF-App](https://play.google.com/store/apps/details?id=org.openfoodfacts.scanner).

Best would be also to **filter** for the **country** where I am living in, and possibly **shops**
also in which I am used to do my shopping.

The website does it all, and you can even query with URL-parameters.

**Try this**, best seen on Firefox dealing with JSON format:

[https://tuttifrutti.alwaysdata.net/fetchAjax/?barcode=29005369&country=en%3Agermany](https://tuttifrutti.alwaysdata.net/fetchAjax/?barcode=29005369&country=en%3Agermany)

[https://tuttifrutti.alwaysdata.net/fetchAjax/?barcode=3596710024087&country=en%3Afrance&store=auchan](https://tuttifrutti.alwaysdata.net/fetchAjax/?barcode=3596710024087&country=en%3Afrance&store=auchan)


### pros and cons
#### up and running ... but slow
The [tuttifrutti](http://tuttifrutti.alwaysdata.net/) is up and running, with no scanning but by entering the barcode so far.

It makes use of the official OFF-database which requires to compute live similar products 
(roughly based on products' attributes *categories_tags* and *nutrition_score_uk*). The computation of similarities
is much too long and may take up to 25 seconds for a single request.

#### serving my needs... and that's all folk!
Hey, what did you expect :) ?

As you may have noticed in the second example provided in section *ante-prosim*, the server is kind enough to deliver (x, y) points for positioning the points in the 
3D.js graph. This is great but not required for all possible needs. Furthermore, this coordinates computation could also be performed on the client-side.

#### based on my own definition of good and similar
What is meant with
- a better product ?
- a similar product ?

Well, as far as I am concerned, and after having looked at the information stored in the OFF-database, I consider that:
- product B is better than product A if the nutrition score of B is better (based on tag *nutrition_score_uk*)
- similarity of product B with product A : the higher the number of common categories between A and B divided by the number of categories of A, the better!


I'm Okay with that! i just need to know about it! It's like computing the probability of occurrence of event B knowing A or its contrary. It's not very intuitive but it's not wrong :).


circles computed on server , not optimal
too many computations live on server, not good for a server
too close to the application, would be better to query/respond immediately

discussion: what is better product, what is similar, discussion off...
=> this engine

### the idea behind Prosim


## samples

[link] data api json

show A / B and B / A

with snapshots from .json and url results


## architecture

### mongo Database


### processes
screenshot

name each cell so that we can easily refer each of them in bugs and so on

## processes

### feeders
nightly OK

delta .. todo

### preparer

### intersect


### enhancements

### known issues

### resources

#### OFF-related from oricdev
* [tuttifrutti](http://tuttifrutti.alwaysdata.net/) uses the official OFF-database to compute live-similaritie.
Due to the inappropriate structure of the database to achieve

#### from OpenFoodFacts
* the [OpenFoodFacts](https://world.openfoodfacts.org) website
* join [slack](https://slack-ssl-openfoodfacts.herokuapp.com/) / `product-comparator` to discuss about your ideas, make suggestions, or get help
* links for requesting [OpenFoodFacts data](https://world.openfoodfacts.org/data) in different formats (json, csv, mongo dumps)
*
