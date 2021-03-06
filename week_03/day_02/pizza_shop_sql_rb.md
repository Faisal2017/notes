## Single models with databases

##### Objectives

- Understand how Ruby talks to a database
- Know that an object maps to a row in the db table
- By the end of this lesson you should be able to create your own single model CRUD app (Ruby and SQL)

### Introduction

For the last few weeks we have setup programs that have been amazing and powerful but limited to the fact that when you run them and the script stops executing all the data you have created disappeared into thin air.

This week we are going to increase the power of our apps by learning and using persistence and talking to a database.

This way we can:

- gather large amounts of data over time
- we can perform processing on existing data
- add and create new data as well as updating our old i.e CRUD!

### Pizza Ordering software

Today we are going to introduce this idea of persistence by creating software that tracks pizza orders for Papa Tyno's Pizzas!

We want to be able to:

- Create new orders
- Read/Find existing orders
- Update existing orders
- Delete existing orders

Up until now most of this functionality would have been pretty hard to accomplish as we only had the computer's memory to use.

First of all though, we will setup our model and object - just like we have done many times in Week 2.

## Pizza/DB model

We are going to setup a model and it's role will be to speak to the database. We have created many models which are tested and each small method did something which we knew would work.

We could do the same for this Pizza DB model but it's more hassle than it's worth to test database models. It can be done but we aren't going to.

What we want to do:

1. Setup a PizzaOrder model
2. Create readers for our instances:
	- first_name
	- last_name
	- topping
	- quantity

In terminal:

```bash
#terminal

mkdir models
touch models/pizza_order.rb
```

We're going to do something new here. Previously, we have been passing in multiple parameters to our models. This is sad since it's extremely hard to remember the order they have to be passed in, and it's not the 'ruby' way. We'll also see a similar pattern in JavaScript when we come to it.

Basically, we will pass on argument, and that argument will be a hash! This will contain all the data we need to make the object.

```ruby
# pizza_order.rb

class PizzaOrder

  attr_accessor :first_name, :last_name, :topping, :quantity

  def initialize( options )
    @first_name = options['first_name']
    @last_name = options['last_name']
    @topping = options['topping']
    @quantity = options['quantity'].to_i
  end

end
```
Now in a bit when we start using th DB, you'll see why this is super useful.

Let's make a little file where we can play with our models.

```bash
#terminal

touch console.rb
```

This is where we will 'new up' some instances of the pizza orders.

```ruby
# console.rb

require('pry-byebug')
require_relative('models/pizza_order')

 order1 = PizzaOrder.new({ 'first_name'=> 'Luke', 'last_name'=> 'Skywalker', 'topping'=> 'Napoli', 'quantity'=> '1'})

 order2 = PizzaOrder.new({ 'first_name'=> 'Darth', 'last_name'=> 'Vader', 'topping'=> 'Quattro Formaggi', 'quantity'=> '1'})
 
 binding.pry
 nil
```

exit quit - our pizza orders are gone!

## Adding a database

Great, now we can create instances of PizzaOrders and we can create as many as we want but this is manual and time consuming. It would be nice to start talking to a database to handle all of our records.

#### Mapping an instance to a table row

We have a PizzaOrder instance here with all the information we need about that pizza order. If we wanted to save a record of our pizza order, it would make sense to take those instance variable values and create a new row in our database table.

We will:

- Require a gem, 'pg', which allows us to:
	- Make a connection to a database
	- Execute SQL on that database
- Create an instance method, let's call it 'save', but it could be called anything e.g. persist_pizza etc.
- Write our sql in our method and execute it
- New up a new instance and invoke the save instance method

We can then check our table and see if it is has persisted.

### Creating a Database and table

In Terminal:

```bash
#terminal

createdb pizza_shop

mkdir db
touch db/pizza_shop.sql

```

in .sql we want to create our table to store our pizza orders. What do we want to save?

- first_name
- last_name
- topping
- quantity

Basically all properties that we created in our PizzaOrder class. We want to take these seperate inputs and save them as a new row in our database.

Let's chuck in an ID as well so we can differentiate between the orders.

```sql
# db/pizza_shop.sql

DROP TABLE IF EXISTS pizza_orders;

CREATE TABLE pizza_orders (
  id SERIAL4 PRIMARY KEY,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  topping VARCHAR(255),
  quantity INT2
);

```

In terminal:

```bash
#terminal

  psql -d pizza_shop -f db/pizza_shop.sql
```

- -d; database select
- -f; file to run in context of selected database

### The PG gem

We first of all need to require a gem called PG. This gem handles our connection to a database and executes SQL for us.

```bash
# terminal

gem install pg
```

We now need to include this gem in our code:

```ruby
# models/pizza_order.rb

require( 'pg' )
```

### Creating our first database method

```ruby
# models/pizza_order.rb

  def save()
    db = PG.connect( { dbname: 'pizza_shop', host: 'localhost' } )
    sql = 
      "INSERT INTO pizza_orders
      (first_name,last_name,topping,quantity) 
      VALUES 
      ('#{ @first_name }','#{ @last_name }','#{ @topping }',#{ @quantity });"
    db.exec(sql)
    db.close()
  end
```

#### What's happening here?

1. In our method we create a connection to our DB and save it in a variable.
2. We save to a variable a sql command in a string.
3. We execute that using the db method .exec before finally closing the connection.
4. We close the connection - we don't want to leave it open.

Our .save method is an instance method. Meaning we can use it on any object Pizza we new up.

We can do this for one of our pizzas in our console file.

```ruby
# console.rb

order1.save()
```
Run console.rb from terminal

exit from pry and let's access our database.

In terminal:

```ruby
psql -d pizza_shop
select * from pizza_orders;

```

Brilliant, we have mapped a Ruby instance to a SQL table row.

Note that only order1 has been saved.

## Reading from the database

Now, we want to go the other way. We want to:

- Read/take from our db table
- map them to Ruby objects

Class discussion: Let's say I wanted to get all the table rows from the database. Using the existing PizzaOrder model, how could I do this. Specifically:

- We created a save method for an instance; one individual object
- Is it right to create an instance method to get all the records?
- Is it wrong that one individual should be able to get all the others?

Ideally, it's not an instance's control to get all the records from a database. But it could be the class. We could ask PizzaOrder to go and grab all the records. We can create a class method.

### PizzaOrder.all()

We can create a class method like so:

```ruby
# models/pizza_order.rb

  def self.all()
    db = PG.connect( { dbname: 'pizza_shop', host: 'localhost' } )
    sql = "SELECT * FROM pizza_orders;"
    orders = db.exec(sql)
    db.close()
    return orders
  end
```

Let's run pizza_order.rb in terminal. Now we can invoke our newly created class method. This gives us an opportunity to see what the PG gem returns from the database.

```ruby
  orders = PizzaOrder.all()
```

It's a PG::Result object. We know what object are. ntuples refers to the number of rows of  data it's taken from the db.

If we run ``` PizzaOrder.all().first() ``` we get a hash of the first row of data. So this PG::Result is an array of hashes. Each individual hash is a row in the table.

### Mapping all the rows to objects

So we know we have, essentially, an array of hashes. We want to convert this into an array of PizzaOrder objects. How can we do this?

We need to map them to a new Pizza Object! This is 
```ruby
# models/pizza_order.rb

 def self.all()
 ...
 return orders.map { |order| PizzaOrder.new( order ) }
 end
```

Map returns a new array and we pass in each hash to create a new pizza instance into the new array.

!!!!! This is only possible because we used our super cool options pattern in the initialize method !!!!!!!

Now if we run this console.rb script and type ``` PizzaOrder.all() ``` We get an array with one item in it. Let's take a look at order2.

This is just in memory but we have our .save() so let's save pizza2. Now if we run PizzaOrder.all() we have multiple items in our array.

#### Adding the id

In our database return we have the ID in the hash, it would be cool to to have that information in our PizzaOrder instances as well.

```ruby
# models/pizza_order.rb

  attr_accessor :first_name, :last_name, :topping, :quantity
  attr_reader :id #new
  #initialize
  @id = options['id'].to_i if options['id']

```

This is fine for when we get all of the orders in all() but we would also like to update the object we have when we save - it currently doesn't have an id.

```ruby
# models/pizza_order.rb

# save method
 #same as before
 sql = 
    "INSERT INTO pizza_orders
    (first_name, last_name, topping, quantity)
    VALUES
    ('#{@first_name}', '#{@last_name}', '#{@topping}', 
      #{@quantity}) RETURNING *;"
 @id = db.exec(sql)[0]["id"].to_i
 db.close
```


Remember, the ID of an object is extremely useful - it's the only unique identifier we have between the rows.

## Deleting All

Everytime we run our console file, we are adding a new order. Let's clean out the database everytime we run it!

```ruby
# models/pizza_order.rb

  def self.delete_all()
   	 db = PG.connect( { dbname: 'pizza_shop', host: 'localhost' } )
    sql = "DELETE FROM pizza_orders;"
    db.exec(sql)
    db.close
  end
```

## Deleting One

Delete probably wants to live on a pizza_order instance, although we could do PizzaOrder.delete(1) and remove it by it's id. However, if we have the object already it would be nice to just use it.

```ruby
# models/pizza_order.rb

  def delete()
    db = PG.connect( { dbname: 'pizza_shop', host: 'localhost' } )
    sql = "DELETE FROM pizza_orders WHERE id = #{@id}"
    db.exec(sql)
    db.close
  end
```

## Updating

Lastly, we might want to update a model. When we do changes like this:

```ruby
# console.rb
order1.first_name = "Fred"
```
This is sitting temporarily on the little model in memory. No SQL has run to cause the database to update the entries, so it gets lost if we exit pry.

```ruby
# models/pizza_order.rb

 def update()
    db = PG.connect( { dbname: 'pizza_shop', host: 'localhost' } )
    sql = "UPDATE pizza_orders 
          SET (first_name, last_name, topping, quantity) =
          ('#{@first_name}', '#{@last_name}', '#{@topping}', #{@quantity} ) 
          WHERE id = #{@id};"
    db.exec(sql)
    db.close
  end
```



