Feature: Delivery Service

  Scenario: Delivery Service accepts an order
    Given a disciple "user5" has an order ready for delivery
    When the rider "user5" accepts the order
    Then the order status should be "OUT_FOR_DELIVERY"

  Scenario: Delivery Service completes an order
    Given a disciple "user6" has an order out for delivery
    When the rider "user6" completes the order
    Then the order status should be "DELIVERED"