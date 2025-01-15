Feature: Platform Security Kitchen Service

Scenario: Dr. Evil tries to accept an order
  Given a username as "man" and a password as "chu" with privileges "order.CRUD"
  When accepts an order then authentication fails

Scenario: Dr. Evil tries to notify an order is ready
  Given a username as "man" and a password as "chu" with privileges "order.CRUD"
  When notifies an order then authentication fails

Scenario: Dr. Evil tries has stolen user credentials and attempts accepting an order
  Given a username as "orderUser1" and a password as "orderPassword1" with privileges "order.CRUD"
  When accepts an order then authorization fails

Scenario: Dr. Evil tries has stolen user credentials and attempts notifying an order
  Given a username as "orderUser1" and a password as "orderPassword1" with privileges "order.CRUD"
  When notifies an order then authorization fails

Scenario: Dr. Evil hacked the auth service and created user without privileges and tries accepting an order
  Given a username as "hackedChef1" and a password as "hackedPassword1" with privileges ""
  When accepts an order then authorization fails

Scenario: Dr. Evil hacked the auth service and created user without privileges and tries notifying an order
  Given a username as "hackedChef1" and a password as "hackedPassword1" with privileges ""
  When notifies an order then authorization fails