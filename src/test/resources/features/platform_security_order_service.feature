Feature: Platform Security Order Service

Scenario: Dr. Evil tries to create an order, but fails
  Given a username as "man" and a password as "chu"
  When a disciple creates an order with building "1" and room number "102" and login fails

Scenario: Dr. Evil tries stealing credentials, and attempting order content but fails
  Given a username as "user" and a password as "password"
  And a disciple "user1" creates an order with building "1" and room number "102"
  And an invalid orderId
  When the disciple adds 2 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE" and attempt fails

Scenario: Dr. Evil tries creating large orders, but fails
  Given a username as "user" and a password as "password"
  And a disciple "user1" creates an order with building "1" and room number "107"
  When the disciple adds 100 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE" and system complains large order

Scenario: Dr. Evil tries to make multiple orders with the login he got, but fails
  Given a username as "user1" and a password as "password1"
  And a disciple creates an order with building "1" and room number "108"
  And the disciple "user1" adds 3 pancake of type "DARK_CHOCOLATE_WHIP_CREAM_PANCAKE"
  And the disciple "user1" completes the order
  Then a disciple "user1" creates an order with building "1" and room number "108" and system complains about ongoing order

Scenario: Dr Evil tries creating an order with invalid delivery information and fails
    Given a username as "user1" and a password as "password1"
    When a disciple "user1" creates an order with building "1001" and room number "109" and system complains about invalid delivery information