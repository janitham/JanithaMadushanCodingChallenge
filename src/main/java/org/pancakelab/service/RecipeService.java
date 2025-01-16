package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.Set;

public interface RecipeService {
    void addRecipe(User user, PancakeRecipe recipe)throws PancakeServiceException;
    void removeRecipe(User user, PancakeRecipe recipe)throws PancakeServiceException;
    void updateRecipe(User user, String name, PancakeRecipe recipe)throws PancakeServiceException;
    void exits(User user, PancakeRecipe recipe) throws PancakeServiceException;
    Set<PancakeRecipe> getRecipes(User user) throws PancakeServiceException;
}
