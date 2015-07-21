

# About Spann #

_Spann_ is an extension library for the [Spring Framework](http://www.springsource.org).
It provides an API for advanced configuration of spring beans based on annotation or any other class metadata,
without (almost) any XML. It's target is to **reduce boilerplate code and configuration** and it reaches **way further the native annotation support of Spring**.

_Spann_ can be seen as a counterpart API to the [XML Schema extension API](http://static.springsource.org/spring/docs/3.0.3.RELEASE/spring-framework-reference/html/extensible-xml.html)
(introduced in Spring 2.0): while the latter simplifies XML configuration via XML Schemas
and allows creating complex bean structures with simple XML grammar, _spann_ simplifies bean configuration
by using annotations or any other class metadata (classes, generics, fields, methods).

The spann-API is very straightforward and requires practically no configuration to introduce
new annotations: you just create the Annotation interface, implement the `MetadataVisitor` interface to process it and glue them together using the `@Visitor` annotation.

Spann adds **meta programming** capabilities to the spring container without (almost) any bytecode engineering.

## But is'nt there a native annotation support in spring? ##

Spring's native annotation support is limited to very simple usecases. In spring 2.5, four
annotations are predefined for different types of beans (@Component, @Service, @Repository and @Controller).

For Spring 3.0, further annotations were introduced, but spring is still missing an API to
allow advanced bean configuration via arbitrary annotations or any other class metadata.
Without _spann_, using custom annotations to configure beans (beyond the basic create, inject aso)
is very demanding and requires knowledge of container internals.

# How it works : A bird's-eye view #

Similar to spring's component scan, _spann_ scans the classpath for classes under selected packages
(filtered by resource name pattern) and creates **class metadata** objects.
Instead of processing this metadata itself (like spring does), _spann_ passes this
metadata to **`MetadataVisitor`s**. Through the `ScanContext`, `MetadataVisitor`s can create arbitrary
bean definitions.

The `ScanContext` also provides an API for sharing references to `BeanDeifnition`s between
`MetadataVisitor`s. This allows simple implementation of **annotation composition**.

Spann is non-intrusive, works well with spring's native annotation support and is simple to use
yet very flexible and powerful.

To get started take a look at [the spann-spring guide](SpannSpring.md).

**The [spann-metadata](SpannMetadata.md) Module**

The [spann-metadata](SpannMetadata.md) module provides an API to access the complete class metadata
information (internally using [ASM](http://asm.ow2.org/)), without loading the classes.

Main features:

  * Simple API to access class metadata, including Class, Methods, Fields, Generics and Annotations.
  * Rule based class metadata loading allows pre-loading a small set of required meta data.
  * Transparent lazy loading on demand of any metadata not loaded previously, recursivly loading metadata of any class referenced by any metadata (super-classes and interfaces, field types, annotation types etc).
  * Portable across different ASM versions. Spann-metadata uses a thin adapter layer on top of ASM. This allows it to use different versions of ASM: it is independent of spring and can be used with (almost?) any ASM version; when used with spring it uses the spring-asm implementation, without introducing new dependencies.

The [spann-metadata](SpannMetadata.md) module has its own documentation [here](SpannMetadata.md).



# Examples of spann uses #

**JPA**

> The following example is implemented in [spann-orm](SpannOrm.md).
> The example shows an abstract UserDao which will be completly implemented by _spann_.
> This is done via CGLib (mostly by spring itself) and is very efficient at runtime.

> Spann-dao is inspired by the  excellent 'polyforms' library. Polyforms provides similar
> functionality but relies on aspects. It uses XML and implements solutions for specific usecases.
> _spann_ on the other hand provides an API for creating beans via annotations and does not use
> aspects, which pays in terms of performance and avoiding bean instantiation order conflicts.

```
@Dao
public interface UserDao extends BaseDao<User,Long> {

  // will perform "FROM User WHERE username = ?"
  @ByMethodName
  User findByUsername( String username );

  // will perform "FROM User WHERE enabled = ?"
  // with the given position
  @ByMethodName
  List<User> findByEnabled( boolean enabled , QueryPosition pos );

  // simple jpql
  @Jpql("FROM User AS u WHERE email = ?")
  User getUserByEmail( String email );

  // More advanced usage with MessageFormat arguments in the jpql.
  // calling findByRegdateOp( "username" , "<" , date );
  // will perform 'FROM User AS u WHERE u.regdate < ? ORDER BY u.username'
  @Jpql("FROM User AS u WHERE u.regdate {1} ? ORDER BY u.{0}")
  List<User> findByRegdateOp( String sortfield , String regdateOp, Date minRegdate );

...

```

> [spann-orm](SpannOrm.md) contains many other features, you can take a look [here](SpannOrm.md)

**Synthetic adapters**

> The following example can be very easily implemented using the `@SyntheticAdapter` meta-annotation.

> The `@SyntheticAdapter` Meta-Annotation is used to create an adapter bean, implementing an abstract class or an interface, to delegate
> any call to the abstract method to the annotated method. The adapter can even be automatically configured according to the method signature.

> This is done without using aspects, thus avoiding runtime overhead and bean initialization order problems.

```
@Component
public class MyValidators {

  @Autowired
  private SomeColaborator foo;

  @Validator(adapterBeanName="personValidator")
  public void validate( Person person , Errors errors ) { ... }

  @Validator(adapterBeanName="addressValidator")
  public void validate( Address address , Errors errors ) { ... }

  ...

} 
```

**JMS**

> This is not implemented yet, but shows the intent...

> With XML, configuration of JMS listener container is quite cumbersome,
> with _spann_ it can be as simple as:
```
@Service
@Implement
public abstract class MyListener {

  @JmsListener(queue="queue-name")
  public void doSomethingWithMessage( TranslatedMessageObject in ) {
    // process message...
    ...
    // and queue for further processing... 
    sendSomeMessage( transformed );
  }
  
  @JmsDispatcher(queue="other-queue-name")
  public abstract sendSomeMessage( SomeOtherMessageObject out ); 
  
}
```