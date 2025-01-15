Feature: Order Service

  Scenario: Disciple creates an order, and haven't completed it yet
    Given a username as "user1" and a password as "password1" with privileges "kitchen.CRUD,order.CRUD,delivery.CRUD"
    And a disciple creates an order with building "1" and room number "101"
    When the disciple "user1" adds 1 pancake of type "DARK_CHOCOLATE_PANCAKE"
    Then the order status should be "CREATED"

  Scenario: Disciple cancels the order
    Given a username as "user2" and a password as "password2" with privileges "kitchen.CRUD,order.CRUD,delivery.CRUD"
    When a disciple creates an order with building "1" and room number "103"
    And the disciple "user2" adds 3 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_PANCAKE"
    And the disciple "user2" cancels the order
    Then the order should be removed from the database
    And the order status should be "CANCELLED"