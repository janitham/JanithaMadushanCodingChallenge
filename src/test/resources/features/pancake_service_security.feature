Feature: Platform Security

Scenario: Dr. Fu Man Chu tries to hack the delivery queue, but fails
  Given an invalid orderId
  When the orderId is added to the delivery queue
  And delivery partner is available for the delivery
  Then the system should reject the orderId

Scenario: Dr. Evil tries to create an order, but fails
  Given a username as "man" and a password as "chu"
  When a disciple creates an order with building "Main" and room number "102" and login fails

Scenario: Dr. Evil tries stealing credentials, and attempting order content but fails
  Given a username as "user" and a password as "password"
  And a disciple creates an order with building "Main" and room number 102
  And an invalid orderId
  When the disciple adds 2 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE" and attempt fails

Scenario: Dr. Evil tries creating large orders, but fails
  Given a username as "user" and a password as "password"
  And a disciple creates an order with building "Main" and room number 107
  When the disciple adds 100 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE" and system complains large order