package it.near.sdk.Recipes.Models;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Recipes.Models.PulseFlavor;
import it.near.sdk.Recipes.Models.ReactionFlavor;

/**
 * @author cattaneostefano
 */
public class Recipe extends Resource {

    @SerializeName("name")
    String name;
    @SerializeName("pulse_ingredient_id")
    String pulse_ingredient_id;
    @SerializeName("pulse_slice_id")
    String pulse_slice_id;
    /*@SerializeName("operation_ingredient_id")
    String operation_ingredient_id;
    @SerializeName("operation_slice_id")
    String operation_slice_id;*/
    @SerializeName("reaction_ingredient_id")
    String reaction_ingredient_id;
    @SerializeName("reaction_slice_id")
    String reaction_slice_id;
    @Relationship("pulse_flavor")
    PulseFlavor pulse_flavor;
    /*@Relationship("operation_flavor")
    OperationFlavor operation_flavor;*/
    @Relationship("reaction_flavor")
    ReactionFlavor reaction_flavor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPulse_ingredient_id() {
        return pulse_ingredient_id;
    }

    public void setPulse_ingredient_id(String pulse_ingredient_id) {
        this.pulse_ingredient_id = pulse_ingredient_id;
    }

    public String getPulse_slice_id() {
        return pulse_slice_id;
    }

    public void setPulse_slice_id(String pulse_slice_id) {
        this.pulse_slice_id = pulse_slice_id;
    }

    /*public String getOperation_ingredient_id() {
        return operation_ingredient_id;
    }

    public void setOperation_ingredient_id(String operation_ingredient_id) {
        this.operation_ingredient_id = operation_ingredient_id;
    }

    public String getOperation_slice_id() {
        return operation_slice_id;
    }

    public void setOperation_slice_id(String operation_slice_id) {
        this.operation_slice_id = operation_slice_id;
    }*/

    public String getReaction_ingredient_id() {
        return reaction_ingredient_id;
    }

    public void setReaction_ingredient_id(String reaction_ingredient_id) {
        this.reaction_ingredient_id = reaction_ingredient_id;
    }

    public String getReaction_slice_id() {
        return reaction_slice_id;
    }

    public void setReaction_slice_id(String reaction_slice_id) {
        this.reaction_slice_id = reaction_slice_id;
    }

    public PulseFlavor getPulse_flavor() {
        return pulse_flavor;
    }

    public void setPulse_flavor(PulseFlavor pulse_flavor) {
        this.pulse_flavor = pulse_flavor;
    }

    /*public OperationFlavor getOperation_flavor() {
        return operation_flavor;
    }

    public void setOperation_flavor(OperationFlavor operation_flavor) {
        this.operation_flavor = operation_flavor;
    }*/

    public ReactionFlavor getReaction_flavor() {
        return reaction_flavor;
    }

    public void setReaction_flavor(ReactionFlavor reaction_flavor) {
        this.reaction_flavor = reaction_flavor;
    }

}
