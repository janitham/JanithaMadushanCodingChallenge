Feature: Platform Security

Scenario: Dr. Fu Man Chu tries to hack the delivery queue, but fails
  Given an invalid orderId
  When the orderId is added to the delivery queue
  And delivery partner is available for the delivery
  Then the system should reject the orderId