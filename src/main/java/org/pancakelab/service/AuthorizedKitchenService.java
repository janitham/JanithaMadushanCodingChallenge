package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Privileges;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

/**
 * Service that provides authorized kitchen operations.
 * This decorator class ensures that only authenticated and authorized users can access the kitchen service.
 */
public class AuthorizedKitchenService implements ChefService, RecipeService {
    public static final String KITCHEN_RESOURCE_NAME = "kitchen";
    private static final String RECIPE_RESOURCE_NAME = "recipe";
    private final KitchenServiceImpl chefService;
    private final AuthenticationService authenticationService;

    /**
     * Constructs an AuthorizedKitchenService with the specified kitchen and authentication services.
     *
     * @param kitchenService        the kitchen service to delegate to
     * @param authenticationService the authentication service to use for user authentication
     */
    public AuthorizedKitchenService(
            final KitchenServiceImpl kitchenService,
            final AuthenticationService authenticationService
    ) {
        this.chefService = kitchenService;
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticates the specified user.
     *
     * @param user the user to authenticate
     * @throws PancakeServiceException if the user cannot be authenticated
     */
    private void authenticateUser(User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
    }

    /**
     * Returns a map of orders for the specified user.
     *
     * @param user the user whose orders are to be viewed
     * @return a map of orders
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, KITCHEN_RESOURCE_NAME, Privileges.READ.getCode());
        return chefService.viewOrders(user);
    }

    /**
     * Accepts the specified order for the specified user.
     *
     * @param user    the user accepting the order
     * @param orderId the ID of the order to accept
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, KITCHEN_RESOURCE_NAME, Privileges.CREATE.getCode());
        chefService.acceptOrder(user, orderId);
    }

    /**
     * Notifies the completion of the specified order for the specified user.
     *
     * @param user    the user notifying the order completion
     * @param orderId the ID of the order to notify completion
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void notifyOrderCompletion(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, KITCHEN_RESOURCE_NAME, Privileges.UPDATE.getCode());
        chefService.notifyOrderCompletion(user, orderId);
    }


    /**
     * Adds a new recipe for the specified user.
     *
     * @param user   the user adding the recipe
     * @param recipe the recipe to add
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void addRecipe(User user, PancakeRecipe recipe) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, RECIPE_RESOURCE_NAME, Privileges.CREATE.getCode());
        chefService.addRecipe(user, recipe);
    }

    /**
     * Removes the specified recipe for the specified user.
     *
     * @param user   the user removing the recipe
     * @param recipe the recipe to remove
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void removeRecipe(User user, PancakeRecipe recipe) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, RECIPE_RESOURCE_NAME, Privileges.DELETE.getCode());
        chefService.removeRecipe(user, recipe);
    }

    /**
     * Updates the specified recipe for the specified user.
     *
     * @param user   the user updating the recipe
     * @param name   the name of the recipe to update
     * @param recipe the updated recipe
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void updateRecipe(User user, String name, PancakeRecipe recipe) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, RECIPE_RESOURCE_NAME, Privileges.UPDATE.getCode());
        chefService.updateRecipe(user, name, recipe);
    }

    /**
     * Checks if the specified recipe exists for the specified user.
     *
     * @param user   the user checking the recipe
     * @param recipe the recipe to check
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void exits(User user, PancakeRecipe recipe) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, RECIPE_RESOURCE_NAME, Privileges.READ.getCode());
        chefService.exits(user, recipe);
    }

    /**
     * Returns a set of recipes for the specified user.
     *
     * @param user the user whose recipes are to be retrieved
     * @return a set of recipes
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public Set<PancakeRecipe> getRecipes(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, RECIPE_RESOURCE_NAME, Privileges.READ.getCode());
        return chefService.getRecipes(user);
    }
}
