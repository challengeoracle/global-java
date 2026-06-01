# Regras de sincronização

O OffPay funciona offline-first.

Pedido pendente de sincronização significa que a operação ainda não chegou ao backend.

Pedido sincronizado significa que o backend recebeu e registrou a operação.

Se o pedido foi criado offline, o pagamento só deve ser considerado definitivo após a sincronização e processamento pelo Payment Service.

Se houver inconsistência entre pedido e pagamento, explique com cautela.

Não afirme que um pedido offline foi pago se o contexto ainda indicar pagamento pendente.