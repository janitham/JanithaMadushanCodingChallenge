Feature: Pancake Delivery Service

  Scenario: Disciple creates an order, and haven't completed it yet
    Given a disciple "user1" creates an order with building "1" and room number "101"
    When the disciple "user1" adds 1 pancake of type "DARK_CHOCOLATE_PANCAKE"
    Then the order status should be "CREATED"

  Scenario: Disciple cancels the order
    Given a disciple "user2" creates an order with building "1" and room number "103"
    When the disciple "user2" adds 3 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_PANCAKE"
    And the disciple "user2" cancels the order
    Then the order should be removed from the database
    And the order status should be "CANCELLED"

  Scenario: Chef accepts the order
    Given a disciple "user3" has a created order
    When the chef "user2" accepts the order
    And the order status should be "IN_PROGRESS"

  Scenario: Chef completes the order
    Given a disciple "user4" has an order in progress
    When the chef "user4" completes the order
    Then the order status should be "READY_FOR_DELIVERY"

  Scenario: Delivery Service accepts an order
    Given a disciple "user5" has an order ready for delivery
    When the rider "user5" accepts the order
    Then the order status should be "OUT_FOR_DELIVERY"

  Scenario: Delivery Service completes an order
    Given a disciple "user6" has an order out for delivery
    When the rider "user6" completes the order
    Then the order status should be "DELIVERED"