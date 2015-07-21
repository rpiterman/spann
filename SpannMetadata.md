
# Introduction #

The spann-metadata module provides an API for scanning java bytecode and accessing it via metadata objects (`ClassMetadata, MehtodMetadata, AnnotationMetatdat` aso).

**Rules based scan** : spann-metadata uses a simple rules engine which allows users to define which metadata will be loaded when "visiting" the java bytecode.

**Lazy loading**: all metadata which was not loaded during the primary scan is lazy-loaded on demand. Lazy loading is transparent to the user and does not require any special API calls.

**Portable** : spann-metadata internally uses ASM to read the bytecode. It uses a thin adapter around ASM to allow different versions of ASM to be used. This is currently used to allow using spann-metadata from within Spring without introducing any dependency to ASM, using the ASM implementation delivered with spring (under `org.springframework.asm`).

# API Overview #

spann-metadata has a single entry point, the `ClassMetadataSource` class.

In short, this is the API to retrieve a `ClassMetadata`:

```

// defines which metadata will be loaded on the first scan
MetadataPathRules rules = Rules.RULES_CLASS_ANNOTATIONS;

// rules for lazy loading metadata
LazyLoadingRulesFactory lazyrules = Rules.LAZY_EAGER_ALL;

// An adapter to the ASM Implementation
ClassReaderAdapter adapter = new AsmClassReaderAdapter(); 
                       // or new SpringClassReaderAdapter();

// Store for class metadata. Can be shared between ClassMetadataSource
MetadataStore store = new MetadataStoreImpl();

// now instantiate and use...
ClassMetadataSource cms = new ClassMetadataSource( rules, lazyrules, adapter , store );

ClassMetadata metadata = cms.getClassMetadata(
  org.spann.metadata.common.ClasspathResource.forClassname( "example.Foo" , cls.getClassLoader() );
```

# Details #

The Metadata interfaces are under `org.spann.metadata.core`. Consult the API-docs for details.

There are 5 main Metadata types : `ClassMetadata`, `MethodMetadata`, `ParameterMetadata`, `FieldMetadata` and `AnnotationMetadat`. The API is quite straight forward.

## Arrays ##

> Any ClassMetadata may represent an array. If it does, its dimensions property will return a value greater than zero.

> In contrary to java reflection API, all other methods reflect the component type's metadata, so `getName()` on the metadata of `String[].class` will return "java.lang.String" and `getMethods()` will return `String.class` methods and not the array's.

## Generics ##

> <strong>TypeMetadata</strong> extends `ClassMetadata` with a `typeArguments` property. Both are interchangeable in many scenarios, so many methods which return `ClassMetadata` may actually return a `TypeMetadata` if generic informattion is present **and** loaded.

> For example, `ClassMetadata.getSuperClass()` returns a ClassMetadata. If the super class has a generic signature and the signature is loaded, it will return a TypeMetadata.

> Same applies to `ClassMetadata.getInterfaces(boolean)`, and for `TypeParameter`'s,`TypeArgument`'s and `GenericType`'s getType() method.

> The following code shows the different generic metadata classes and their semantics.
```

  // ClassMetadata Foo has 1 TypeParameter{name:"X",type:<ClassMetadata of Bar>}.
  // (Also methods may have TypeParameters, like in 'public <T> void getValue( Foo<T> f);')
  public class Foo<X extends Bar>
  
      // Foo's ClassMetadata.superClass is a TypeMetadata with 1 TypeArgument{contextBoundTypeParameter="X"}
      extends Baz<X>

      // the Predicate interface is represented by a TypeMetadata with 1 TypeArgument with   {genericCapture:SUPER,type:<ClassMetadata of Bar>}
      implements Predicate<? super Bar> {

    // The fieldType of field 'value' is a GenericType{contextBoundTypeParameter:"X"}
    // GenericType is also used for method return type or method parameter type.
    private X value;

    // The fieldType of field 'type' is a 
    //  GenericType{
    //    type=TypeMetadata{
    //      name:java.lang.Class,
    //      typeArguments:[ 
    //          TypeArgument{
    //            contextBoundTypeParameter:"X",contextBoundParameterDimentions:1
    //          } 
    //      ]
    //    }
    //  }
    private Class<X[]> type;
  }
```



## Annotations ##

> The annotations metadata API is quite straight forward.

> Two features are worth mentioning here:

> (1) `AnnotationMetadata` distinguishes between explicit annotation attribute values and default values. This information is not available via the java annotation API and may be usefull.

> (2) `AnnotatedElement.findAnnotationPaths(...)` returns a list of paths to a given (meta-)annotation. Meta-Annotations are annotations which annotate annotations. See Spring 3.0 usage of `@Component`.

## Utilities ##

> `AnnotationMetadataSupport`, `ClassMetadataSupport`, `FieldMetadataSupport` and `MethodMetadataSupport` are utility classes with some usefull methods to handle the corresponding metadata objects.