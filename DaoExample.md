
```

@Dao
public interface BookingItemDao extends BaseDao<BookingItem,Long> {

  @ByMethodName
  List<BookingItem> findByOwnerAndState( User owner , BookingItemState state );


  // Will return first result
  @ByMethodName
  BookingItem findByOwnerAndState( User owner , BookingItemState state );


  // Will use JPA getSingleResult() and throw exception if none or more than one...
  @ByMethodName
  BookingItem getByOwnerAndState( User owner , BookingItemState state );


  // ByMethodName supports count, countDistinct, max, min, sum and avg.
  @ByMethodName
  BigDecimal maxAmountByOwner( User owner );

  // The QueryPosition argument will set the JPA Query's firstResult and maxResults
  // properties. It is null safe so users can pass null to return all results.
  @ByMethodName
  List<BookingItem> findByOwner( User owner , QueryPosition pos );

  // Note the MessageFormat argument in the end of the JPQL query.
  @Jpql("FROM BookingItem WHERE account.id = ? ORDER BY date {1}")
  List<BookingItem> findByAccountId( Long accountId , String ascOrDesc , 
    QueryPosition qp );

  // Or with named parameters
  @Jpql("FROM BookingItem WHERE account.id in (:ids)")
  @NamedParameters("ids")
  List<BookingItem> findByAccountIds( Collection<Long> ids );

  // The @Count annotation is pretty neat: it performs a count query
  // before the actual query, and sets the result to the given
  // QueryCount argument.
  //
  // The QueryPositionCount class will adjust the query position
  // if the result if the count query exceeds the current position.
  @Jpql("FROM BookingItem WHERE owner = ?")
  @Count
  List<BookingItem> findByOwner( User owner , QueryPositionCount c );
  
  // an update...
  @Jpql("UPDATE BookingItem AS i SET i.state = ? WHERE i.owner = ? AND i.date < ?" )
  int updateStateByOwnerBefore( BookingItemState newState , User owner , Date date );


  // Will execute the named query 'BookingItem.findByState'
  @NamedQuery
  @NamedParameters("s")
  List<BookingItem> findByState( QueryPosition qp, Collection<BookingItemState> states );
  
}


```