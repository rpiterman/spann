

# Introduction #

The target of spann-orm module is to provide implementation of common ORM related usecases.

Currently implemented are:

  * JPA based DAOs


## Usage ##

To use **spann-orm** you need to:
  * Add it (and spann-spring and spann-metadata) to your class path, see [Maven](Maven.md).
  * Add the spann-spring scan bean to your spring container and set the packages to scan, see [SpannSpring](SpannSpring.md).
  * Create the DAO classes/interfaces.

# JPA DAOs #

The main motivation for this module is DRY. It reduces duplication of both java code and JPQL code. Creating JPA queries with spann-orm is very simple and concise. Maintaining the queries is made easy via unit testing and (half-)automated generation of tests.

JPA DAOs is not a close black box: it is extendable in (almost) any way you can think of. It uses spann-spring's `GenericMethodReplacer` which contains a simple workflow for customizing method execution. This allows developers to customize and extend the `@DaoMethod` for (almost) any JPA Query related use, so if you are missing a feature, you can easily implement it using your own annotation. See example below.

[A Complete Dao exmaple](DaoExample.md)

Spann DAOs are inspired by the excellent Polyforms library. However, spann DAOs cover more usecases (query by method name or jpql), are extendable via the spann-spring API, and has better support for JPA features (better named parameters support).

## `@Dao` ##

> A Dao must be defined in either an interface extending `BaseDao<Entity,PrimaryKey>`
> or in an (optionally abstract) class extending `BaseDaoImpl<Entity,PrimaryKey>`.
```
   @Dao
   public interface UserDao extends BaseDao<User,Long> { ... }
  
   @Dao
   public /* abstract ?*/ class UserDao extends BaseDaoImpl<User,Long> { ... }
```

## `@DaoMethod` ##

> The `@DaoMethod` annotation delegates the annotated method to a `GenericMethodReplacer`.

> The `op` attribute defines the query execution strategy: `getResultList()`, `getSingleResult()` or `executeUpdate()`. The default is to detect the strategy from the method name and its return type:
    * `update...` will execute an update
    * `get...` will get a unique result
    * All other method names will use `getResultList()`; if the method does not return a `Collection` or a `List`, the first element in the result list will be returned ("find first or null").

> The annotations `ByMethodName`, `Jpql` and `NamedQuery` are annotated with `DaoMethod`. So, using it in combination with those annotations is only needed if you wish to use a different op than the default (DETECT).

> <b>Query firstResult and maxResults</b>

> Any method (meta-)annotated by `@DaoMethod` may have an argument of type QueryPosition. This will optionally set the firstResult and maxResults of the query. The argument handling is null safe. A `null` QueryPosition on call time will be ignored. The same applies to firstResult and maxResults - they may be null and will be ignored.

> See also `@Count` below.

> <b>Dynamic argument mapping</b>

> A important feature of the `@DaoMethod` annotation is the dynamic use of arguments.
> Method arguments are "consumed" by call-context handlers, allowing some arguments to be used as query arguments, other as Jpql replacement (`{0},{1}` etc), and yet others for any other use.

> For example, the `QueryPosition` class, which can be used with any `@DaoMethod`. An argumnet of this
> class will be consumed and not be used as query argument, no mutter in which position it is used.

> Annotation developers can decide which strategy to use to identify arguments to be consumed. Using method argument annotations is also possible, but Spann's preferred strategy is by type. Using parameter annotation makes argument semantic clear only via the documentation or the source code, while using argument type (like QueryPosition) is self documenting.

> <b>Usage</b>

> The `@DaoMethod` annotation requires at least one more annotation which indicates how to create the query (technically spoken: sets the `contextFactory` property of the `GenericMethodReplacer`).

> Currently the annotations `@ByMethodName`, `@Jpql` and `@NamedQuery` are implemented. (Developers can create other custom annotations).

## `@ByMethodName` ##

> The `@ByMethodName` annotations creates a JPQL query from the method name. It can be used for very simple queries which make a single use of each argument with the "=" operator.

> Named parameters are not supported.

> See the API Docs for the supported syntax.

> Some examples of the supported method name syntax: `findAll(), findByName(..), count(), countByName(..), countDistinctNameByCountry(..), avgSallaryByDepartment(..), maxSallaryByPosition(..), sumAmountByDepartment(..)`

> Note that aggregate functions 'By' clause (sumFooByBar) is used as 'WHERE' clause in the generated JPQL, and not as GROUP BY.

## `@Jpql` ##

> Executes a given JPQL query.

> Named parameters are supported via the `@NamedParameters annotation (see below).

> The JPQL may contain simple argument replacements in form of `{0}`, `{1}` etc. indicating a method argument index. This allows great flexibility in creating dymanic queries in which order-by columns or any other token in the query is dynamicaly replaced by a method argument.

> The JPQL Annotation Visitor uses internally MessageFormat to embed the method arguments
> in the query, however **only the simple syntax of {n}** should be used.

> Any arguments used as JPQL Token replacement will be excluded from the query arguments.

> For example:
```
  @DaoMethod
  @Jpql("FROM User AS u WHERE u.role = ?")
  List<User> findUserByRole( String role );

  @DaoMethod
  @Jpql("FROM User AS u WHERE u.role in :roles)
  @NamedParameters( "roles )
  List<User> findUserByRoles( Collection<String> roles , QueryPosition qp );

  @DaoMethod
  @Jpql("FROM Department AS d WHERE d.budget {3} ? ORDER BY d.{1} {2}")
  List<Department> findByBadget( 
    QueryPosition qp,  // #0 , will be consumed to set firstResult/maxResults.
    String orderField, // will replace {1}
    String ascOrDesc,  // will replace {2}
    String budgetOp,   // will replace {3}, can be any Jpql Op, such as "=","!=" etc.
    BigDecimal budget);// will be used as query argument 
```

## `@NamedQuery` ##

> The `@NamedQuery` will use a defined named query. If no query name is given, the default is `<entity-simple-name.<method-name>`, for example `"Author.findByFoo"`.

## `@NamedParameter` ##

> Will map the method arguments used as query parameters to the given named parameters.

> Not supported with `@ByMethodName`.
```
     @Jpal("FROM Foo as f WHERE f.bar = :bar AND f.baz in (:baz)")
     @NamedParameter({"baz","bar")
     List<Foo> findByBarAndBaz( Collection<Baz> baz , Bar bar );
```

## `@Count` ##

> When using `@Jpql` or `@ByMethodName`, the @Count annotation may be used to perform a count query before the actual finder. If used, one argument of type `QueryCount` must be present.

> Note that the Count annotation does not use a full-featured JPQL parser, so for complicated query syntax, it may fail to transform the Jpql to a count query.

> The class `QueryPositionCount` extends `QueryPosition` with a `QueryCount` implementation simplifies Pagination. It allows adjusting the query first result after the count query, if this exceeds the count returned from the database.

> With `@Count` and `QueryPositionCount`, pagination is simple and concise:
```
    QueryPositionCount qpc = new QueryPositionCount( myoffs , pagesize , AdjustOffset.TO_LAST_PAGE );
    List<Foo> l = myDao.findFooByBar( qpc , bar );
    gui.applyOffset( qpc.getFirstResult() );
    gui.applyCount( qpc.getCount() );
```
> Instead of
```
    long count = myDao.countFooByBar( bar );
    if ( myoffs < count ) {
      // TODO adjust offset
      gui.applyOffset( myoffs );
      gui.applyCount( count );
    }
    QueryPosition qp = new QueryPosition( myoffs , pagesize );
    List<Foo> l = myDao.findFooByBar( qp , bar );

```

> This makes (1) an extra count query definition, (2) a count dao method and (3) the extra code to adjust the offset obsolete by a single @Count annotation.

## Testing ##

`DaoTestSupport.createCallbacks(springAppCtx,resolver,packages)` returns a list of `DaoMethodInvoker`s for all daos found in the given packages. The invokers are configured with default argument values for testing the syntax of all dao methods. Simply call invoker.invoke() from your test class to validate that JPA can process the query.

Naturally this will not test the correctness of any query, but will validates it is a legal JPA query and that the entity names and their fields are correct.

The invokers are confgured with default values by argument type, which are provided by the given resolver. The class `MapArgumentResolver` provides a map based resolver implementation which is initialized with many common classes (primitives and their boxed types, dates, calendar aso). For support of further argument types either add types to the `MapArgumentResolver` instance (which is actually a HashMap), or create your own resolver.

Some queries use entities as parameters, and JPA requires these are attached to the current entity manager. The resolver can return a Factory instance to defer the value creation to just before the call. You will have to make sure that `invoker.invoke()` is called after creating the `EntityManager` - for exmaple from inside spring's `JpaTemplate.execute(...)`.

The resolver may also return `DaoTestSupport.SKIP`. If a method argument type is mapped to SKIP, no invoker will be created for that method.

The resolver should never return `null`. Instead, use `DaoTestSupport.NULL`. If a null is returned by the resolver, an IllegalArgumentException is thrown by the `createCallbacks` method.

The `DaoMethodInvoker` gives access to the DAO class, the DAO bean, the method and the arguments, so one can further customize the invocation prior to invoking, if needed. `DaoTestSupport` will contain also other useful methods for filtering, finding and mutating `DaoMethodInvoker`s.

## Creating new annotations ##

<font color='red'>Note this section is outdated</font>

New annotations can be created for new use cases. You need to get familiar with the **spann-metadata** and **spann-spring** API, but these are quite straight forward.

The most tricky part is understanding the `GenericMethodReplacer` and its 2-phase configuration (configuration-time, bean-creation-time and method-call-time).

For example, we can create an annotation which enables us to set dynamic query hints. The following example illustrates this:

```
  public class HintMap extends HashMap<String,Object> { }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
  @Visitor(value=HintSupportVisitor.class,order=Order.AFTER_CREATE)
  public @interface HintSupport {
  }

  public class HintSupportVisitor extends AbstractGenericReplacerAnnotationVisitor {
    public HintVisitor() {
      super( HintSupport.class.getCanonicalName() , true );
    }
  

    @Override
    protected void process(MethodMetadata metadata, ScanContext context, AnnotationPath path,
			BeanDefinitionHolder methodReplacer, BeanDefinitionHolder callContextHandlerListFactoryBean) {
      
      // to access the any annotation attribute you can use:
      // AttributeClass val = path.getAttribute( 0, AttributeClass.class, "attributename", false );
     
      // find the index of the HintMap argument in the annotated method.
      int hintValuePos = MetadataSupport.findParameterByType( metadata, HintMap.class.getCanonicalName() );

      // TODO - check that hintValuePos is >= 0, else throw? quit silently? warn?

      // the CallContextHandlerChainBuilderCallbackImpl will 'consume' the HintMap
      // argument (by index), so it is not treated as query parameter, and add our HintMapHandler
      // to the call context handler chain. This will be performed only once, when spring
      // creates the bean. On method-call-time, only our HintMapHandler will be called.
      GenericMethodReplacerSupport.addCallContextVisitorsBuilderCallback( callContextHandlerListFactoryBean, 0, 
		new CallContextHandlerChainBuilderCallbackImpl( new HintHandler( hintValuePos), hintValuePos );
    }
  }
  
  public class HintMapHandler implements Resolver<Boolean,QueryCallContext> {
    private final int argIndex;

    public HintHandler( int idx ) { 
      this.argIndex = idx;
    }

    // will be called on each method invocation and may mutate the query...
    public Boolean resolve( QueryCallContext ctx ) {
      HintMap v = (HintMap)ctx.getArgument( argIndex );
      if ( v != null ) {
        for ( Entry<String,Object> e : v.entrySet() ) {
          ctx.getQuery().setHint(e.getKey(),e.getValue());
        }
      }
      // do not shortcut execution.
      return false;
    }
  }
		
```

and then use it...

```
  @ByMethodName
  @HintSupport
  List<Foo> findFooByBaz( Baz baz , HintMap hints );
```
and
```
  HintMap hints = new HintMap();
  hints.put( "openjpa.FetchPlan.MaxFetchDepth" , 3 );
  fooDao.findFooByBaz( baz , hints );
```

We can also create any new annotation which is annotated with the new `@HintSupport`, maybe bundle it with other annotations. **spann-spring** will trigger our new `HintSupportVisitor` without any extra configuration.
