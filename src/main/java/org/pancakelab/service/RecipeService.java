package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;

import java.util.Set;

public interface RecipeService {
    void addRecipe(PancakeRecipe recipe)throws PancakeServiceException;
    void removeRecipe(PancakeRecipe recipe)throws PancakeServiceException;
    void updateRecipe(PancakeRecipe recipe)throws PancakeServiceException;
    void exits(PancakeRecipe recipe) throws PancakeServiceException;
    Set<PancakeRecipe> getRecipes();
}
