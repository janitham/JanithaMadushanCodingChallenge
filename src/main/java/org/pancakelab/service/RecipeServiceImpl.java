package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class RecipeServiceImpl implements RecipeService {
    private final ConcurrentSkipListSet<PancakeRecipe> pancakeRecipesRepository;

    public RecipeServiceImpl(ConcurrentSkipListSet<PancakeRecipe> pancakeRecipesRepository) {
        this.pancakeRecipesRepository = pancakeRecipesRepository;
    }

    @Override
    public void addRecipe(PancakeRecipe recipe) throws PancakeServiceException {
        validate(recipe);
        if (!pancakeRecipesRepository.add(recipe)) {
            throw new PancakeServiceException("Recipe already exists.");
        }
    }

    @Override
    public void removeRecipe(PancakeRecipe recipe) throws PancakeServiceException {
        if (!pancakeRecipesRepository.remove(recipe)) {
            throw new PancakeServiceException("Recipe not found.");
        }
    }

    @Override
    public void updateRecipe(String name, PancakeRecipe recipe) throws PancakeServiceException {
        validate(recipe);
        pancakeRecipesRepository.removeIf(r -> r.getName().equals(name));
        pancakeRecipesRepository.add(recipe);
    }

    @Override
    public void exits(PancakeRecipe recipe) throws PancakeServiceException {
    if (!pancakeRecipesRepository.contains(recipe)) {
        throw new PancakeServiceException("Recipe does not exist.");
    }
}

    private void validate(PancakeRecipe recipe) throws PancakeServiceException {
        if (recipe == null) {
            throw new PancakeServiceException("Recipe cannot be null.");
        }
    }

    @Override
    public Set<PancakeRecipe> getRecipes() {
        return new HashSet<>(pancakeRecipesRepository);
    }
}
