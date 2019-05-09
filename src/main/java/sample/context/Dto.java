package sample.context;

import java.io.Serializable;

/**
 * A marker interface to DTO(Data Transfer Object).
 * 
 * <p>You have a role to step over DTO in succession to this interface between layers,
 * and to deal with information, and to enable and carry out the next duty.
 * <ul>
 * <li>Reduction of the communication cost by the report of plural information.
 * <li>The collection of the variable information.
 * <li>Transfer of the domain information.
 * <li>Transfer of the simple value object which does not have a domain logic.
 * </ul>
 */
public interface Dto extends Serializable {

}
