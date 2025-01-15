package org.pancakelab.util;

import org.pancakelab.model.AuthorizationFailureException;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.List;
import java.util.logging.Logger;

public class PancakeUtils {

    public static final String USER_IS_NOT_AUTHORIZED = "User does not have enough privileges to perform this action";
    private static final Logger logger = Logger.getLogger(PancakeUtils.class.getName());

    private PancakeUtils() {
    }

    public static void notifyUser(User user, OrderStatus orderStatus) {
        if (user == null || orderStatus == null) {
            throw new IllegalArgumentException("User and OrderStatus cannot be null");
        }
        logger.info("Notifying %s that the order is %s".formatted(user, orderStatus));
    }

    public static void authorizeUser(User user, String service, Character privilege) throws PancakeServiceException {
        List<Character> userPrivileges = user.getPrivileges().get(service);
        if (userPrivileges == null || !userPrivileges.contains(privilege)) {
            throw new AuthorizationFailureException(USER_IS_NOT_AUTHORIZED);
        }
    }
}
