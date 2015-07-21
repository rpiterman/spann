# spann-spring #

**spann-spring** provides an API for creating and configuring spring beans via annotations.
On top of this API, spann-spring also delivers with some useful Annotations for common use cases.

The annotations included with spann-spring are listed and documented [here](SpannSpringAnnotations.md).

## How spann works ##

Similar to spring's component scan, spann-spring creates `ClassMetadata` object for all classes under
given packages (and matching a given resource name pattern). Instead of processing the `ClassMetadata`
itself, spann passes it to a list of `MetadataVisitor`s, which in turn may configure spring beans via
the `ScanContext`.

## Getting started ##

There are two methods to configure spann: (1) using the xml schema extension and (2) as a
normal spring bean.

### Configuration spann using xml schema extension ###

When using the spann xml schema extension, spann will execute in the XML parse
phase of the spring container initialization.

It is strongly recommended to add `<spann:scan...`  **after** spring's component scan element (if any).

A Minimal configuration would be:

```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:spann="http://os.masetta.com/spann/schema/spann-1.0"
    xsi:schemaLocation="
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd
http://os.masetta.com/spann/schema/spann-1.0 http://os.masetta.com/spann/schema/spann-1.0.xsd">

    <!-- optional -->
    <context:annotation-config/>
    
    <!-- optional -->
    <context:component-scan base-package="package1,package2,..."/>
    
    <spann:scan base-package="package1,package2,..."/>
    
```

### Configuration as BeanFactoryPostProcessor ###

When using spann as normal spring bean, spann will execute as a BeanFactoryPostProcessor.
This phase takes place after XML parsing and involves loading of some application classes.
In some cases, this may cause class loading issues.

Also as a `BeanFactoryPostProcessor`, spann will execute **before** spring's annotation configuration
is processed (with exception of `@Component` and it's sub annotation); hence any bean configured with
spann may use @Autowired and other Annotations used by spring's annotation configuration.

A Minimal configuration as a spring bean would be:

```
  ...
  <bean class="com.masetta.spann.spring.config.SpannBeanFactoryPostProcessor">
       <property name="basePackagesNames" value="package1,package2"/>
  </bean>
```

### Configuration in detail ###

Spann component scan supports the following properties / attributes:

| **xml schema attribute** | _**bean property**_ | **detail** |
|:-------------------------|:--------------------|:-----------|
| base-package             | _basePackagesNames_ | comma separated list of package names to scan. All packages under the given packages will also be scanned recursivley. |
| scoped-proxy-resolver    | _scopeProxyModeResolver_ | a full qualified class name of a ScopeProxyModeResolver implementation. Consult spring documentation for details |
| scoped-proxy-mode        | _-none-_            | one of `'targetClass'`, `'interfaces'` or `'no'`. This is a convenience method to setup a "constant" resolver. |
| resource-pattern         | _resourcePattern_   | Ant style resource pattern to be used under the given packages. The default pattern is '/**.class'.**|
|name-generator            |                     | A full qualified class name ( A BeanNameGenerator implementation ) of a name generator to use for auto generating bean names. The default generator is conform to the spring default name generator. <br />**Note** that this object will be "consulted", but spann will add '#n' to the generated bean names on demand to avoid conflicting bean names. |

**Visitors**

Visitors configuration is optional. If not explicitly set, the `DefaultVisitor` is used (see below).
If Visitors _are_ configured, the DefaultVisitor should be explicitly added if needed.

When using xml schema configuration, visitors can be added with the `spann:visitor` element:
```
    <spann:scan ...>
        <spann:visitor class="..."/>
        <spann:visitor class="..."/>
        ...
    </spann:scan>
```

When configuring spann as a bean, the `metadataVisitors` property should be used.

## The default visitor ##

The default visitor searches classes for the `@Visitor` annotation at any meta annotation level and
delegates to the `MetadataVisitor` defined by that annotation.

If a class is annotated by `@VisitMethods` (at any meta-annotation level), its methods will also be
searched for the @Visitor annotation. The same applies to `@VisitFields` respectively.

The following example defines the Implement annotation
(implemented in spann-spring's base package).
```
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.ANNOTATION_TYPE})
@Visitor(order=Order.CREATE_BEAN , value=ImplementVisitor.class)
@VisitMethods
public @interface Implement {
    
    /** Implement name */
    String value() default "";
    
}
```

When the DefaultVisitor encounters a class annotated by the `@Implement` (meta-) annotation (at any
meta-annotation level), it will create a singleton instance of ImpelementVisitor
(or reuse an already existing instance) and call its visit(...) method:

```
public class ImplementVisitor implements MetadataVisitor<ClassMetadata> {
    public void visit( ClassMetadata metadata, ScanContext context) {
        ...
    }
}
```

### Global vs. delegate `MetadataVisitor` ###

Note that spann uses the `MetadataVisitor<T>` interface to both configure global visitor and delegators of the
`DefaultVisitor`; these  two usecases should however be distinguished:

**Global visitors** (configured in xml) visit **every** class spann scans and visit only
(und thus must have a generic type of) `ClassMetadata`.

**Delegate visitors** are called by the DefaultVisitor **only** for Metadata-Elements
(Class- Method- or FieldMetadata) which reference them via the `@Visitor` (meta-) annotation,
and may process (and thus have a generic type of) ClassMetadata, FieldMetadata or MethodMetadata.

See also `AnnotationPathMetadataVisitor`, a useful base class for implementing `MetadataVisitor`s.

## Annotation Composition ##

A Design goal of spann-spring is to allow annotation composition: the API should allow designing
annotations (and the beans configured as the result of their use) that may be enhanced
by other annotations.

One technical requirement to address this goal is that annotations can be detected in
any meta-annotation level. Class- Field- and MethodMetadata interfaces (in spann-metadata)
supply the API to address this:

```
 List<AnnotationPath> findAnnotationPaths( String annotationCannonicalClassName );
```

A Second requirement addressing this goal is to provide an API through which `MetadataVisitor`s
can easily share references to bean definitions. This is addressed with the _attach_ API of
the `ScanContext` which is explained in detail below.

Lastly, `MetadataVisitor` order is configurable via the `@Visitor` annotation, and convention
constants are available via the `Order` class.

## The _attach_ API ##

As mentioned above, the _attach_ API allows visitors to share references to `BeanDefinition`s.



_Attaching_ a `BeanDefinitionHolder` to a given metadata and to a given scope will
  1. give the bean a certain semantic (role) for the given metadata, and
  1. make the bean unique by class name in the given scope (this is only forced by the attach method
> and does not constrain spring or creation of other beans).

For example, if a visitor needs to create an auxiliary bean which should be referenced
by all invocation to this visitor (e.g. to be injected to other beans created by this visitor),
it can attach it to the global scope.

Or, if a method-annotation visitor needs to create an auxiliary bean which should be referenced only for other "visits" to methods of the same class, it can attach it to the CLASS scope. Later, when visiting other methods in the same class, the attached bean can be obtained from the ScanContext. When visiting methods of other classes, another bean needs to be created for that class.

By convension, the `"main"` role is used for the main bean of any artifact.

### API ###

To attach a BDH, a visitor should call

```
void attach( BeanDefinitionHolder bdh, Metadata metadata , Artifact attachScope, String role );
```

on the ScanContext.

The given BDH is attach to
  1. The given metadata with the given role.
  1. To the given scope with the BDH's full qualified class name.

To retrieve the bean, a visitor may call

```
BeanDefinitionHolder getBeanDefinitionHolder( Metadata metadata, Artifact attachMetadataArtifact , String beanRole );
```
where scope is the Artifact corresponding to the **attach** metadata.

The same method can be used with a different scope and different role to obtain the same bean, namley (watch for the argument names):

```
BeanDefinitionHolder getBeanDefinitionHolder( Metadata metadata, Artifact attachScope , String fqClassName );
```

So the bean can be looked up by either
  1. the attach-metadata-artifact and attach-role, or (2)
  1. by the attach-scope and the bean's classname.

For convenient creating of dependencies, visitors can use

```
VisitorSupport.getOrCreateAndAttach( ScanContext context, Metadata metadata , 
            Artifact scope, String role, Object source , String clazz )
```

to create a bean reference and attach it to a scope and a role. This method will lookup the needed
bean in the given scope. If the bean does not exist, a new bean is created and attached to the given
scope and role. `getOrCreateAndAttach` returns the bean name of the needed bean.