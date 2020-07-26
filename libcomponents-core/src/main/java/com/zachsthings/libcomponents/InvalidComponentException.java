package com.zachsthings.libcomponents;

/**
 * @author zml2008
 */
public class InvalidComponentException extends Exception {
    private static final long serialVersionUID = 6023653129909836161L;
    private final Class<?> componentClass;
    
    public InvalidComponentException(Class<?> componentClass, String message) {
        super(message);
        this.componentClass = componentClass;
    }
    
    @Override
    public String getMessage() {
        return "Component " + componentClass.getCanonicalName() +
                " could not be loaded due to an error in the structure of the component: "
                + super.getMessage();
    }
}
