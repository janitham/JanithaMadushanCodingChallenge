Feature: Platform Security Delivery Service

Scenario: Dr. Evil tries to accept a delivery
  Given a username as "man" and a password as "chu" with privileges "kitchen.CRUD"
  When accepts a delivery then authentication fails

Scenario: Dr. Evil tries to send a delivery
  Given a username as "man" and a password as "chu" with privileges "kitchen.CRUD"
  When sends a delivery then authentication fails

Scenario: Dr. Evil tries has stolen user credentials and attempts accepting a delivery
  Given a username as "kitchenUser1" and a password as "kitchenPassword1" with privileges "kitchen.CRUD"
  When accepts a delivery then authorization fails

Scenario: Dr. Evil tries has stolen user credentials and attempts sending a delivery
  Given a username as "kitchenUser1" and a password as "kitchenPassword1" with privileges "kitchen.CRUD"
  When sends a delivery then authorization fails

Scenario: Dr. Evil hacked the auth service and created user without privileges and tries accepting a delivery
  Given a username as "hackedRider1" and a password as "hackedPassword1" with privileges ""
  When accepts a delivery then authorization fails

Scenario: Dr. Evil hacked the auth service and created user without privileges and tries sending a delivery
  Given a username as "hackedRider1" and a password as "hackedPassword1" with privileges ""
  When sends a delivery then authorization fails