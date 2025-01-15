Feature: Kitchen Service

  Scenario: Chef accepts the order
    Given a disciple "user3" has a created order
    When the chef "user2" accepts the order
    And the order status should be "IN_PROGRESS"

  Scenario: Chef completes the order
    Given a disciple "user4" has an order in progress
    When the chef "user4" completes the order
    Then the order status should be "READY_FOR_DELIVERY"