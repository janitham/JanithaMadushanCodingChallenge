Feature: Pancake Delivery Service

  Scenario: Disciple creates an order, and haven't completed it yet
    Given a disciple "user1" creates an order with building "Main" and room number 101
    When the disciple "user1" adds 1 pancake of type "DARK_CHOCOLATE_PANCAKE"
    Then the order status should be "CREATED"

  Scenario: Disciple completes the order, and it is ready for delivery
    Given a disciple "user2" creates an order with building "Main" and room number 102
    When the disciple "user2" adds 2 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE"
    And the disciple "user2" completes the order
    Then the order status should be "READY_FOR_DELIVERY"

  Scenario: Disciple completes the order, and it has delivered
    Given a disciple "user3" creates an order with building "Main" and room number 102
    And the disciple "user3" adds 2 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE"
    And the disciple "user3" completes the order
    When delivery partner is available for the delivery
    Then the order status should be "DELIVERED"

  Scenario: Disciple cancels the order
    Given a disciple "user4" creates an order with building "Main" and room number 103
    When the disciple "user4" adds 3 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_PANCAKE"
    And the disciple "user4" cancels the order
    Then the order should be removed from the database
    And the order status should be "CANCELLED"