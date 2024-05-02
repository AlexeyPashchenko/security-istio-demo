# Описание:
Сервис для демонстрации связки istio + keycloak + spring boot(java).
Локальный swagger: http://localhost:8080/demo/swagger-ui/index.html#/

0. Установить Minikube и Docker.
1. Запускаем Minikube:
   ```
   $   minikube start --driver=docker --cpus 4 --memory 17g --force-systemd=true --addons=ingress --addons=ingress-dns
   ```
   ```
   $   minikube addons enable ingress
   ```
   ```
   $   minikube addons enable ingress-dns
   ```

   *Для проверки статуса можно выполнить:
   ```
   $   minikube status
   ```

2. Устанавливаем Istio:
   ```
   $   istioctl install --set profile=demo -y
   ```
   ```
   $   minikube addons enable istio
   ```
* Для доступа к локальному кластеру буду использовать инструмент Lens
  2.1. Проверяем в Lens наличие istio неймспейса и istio подов

3. Устанавливаем keycloak через helm
   ```
   $    helm install my-release --set auth.adminUser=admin --set auth.adminPassword=admin  oci://registry-1.docker.io/bitnamicharts/keycloak
   ```
   3.1*. Ждём три минуты пока поднимется Keycloak и БД Keycloak'а
   3.2*. Проверяем в Lens наличие Keycloak и настраиваем port forwarding 8080->4040

4. Настройка Keycloak:
 - создаём реалм istio
 - создаём клиент "OpenID Connect" (название istio. остальное по умолчанию, главное выбрать тип клиента "OpenID Connect")
 - добавляем роли "ROLE_USER" и "ROLE_ADMIN".
 - создаем пользователей test-user и test-admin
   (пароли не временные + email + Email verified + имя + фамилия, иначе есть риск получить ошибку {"error":"invalid_grant","error_description":"Account is not fully set up"}!)
 - назначаем пользователям соответствующие роли

5*. Это необязательный шаг, нужен если вы решили использовать своё приложение, а не использовать образ с моим приложением
*Как сбилдить и запушить образ в docker hub?
 - авторизуемся $ docker login
 - проверяем, что находимся в папке с файлом Dockerfile
 - выполняем команды:
   ```
   $ docker buildx build --platform=linux/arm64/v8 -t 234423/security-istio-demo:v1 .
   ```
   ```
   $ docker push 234423/security-istio-demo:v1
   ```

6. Развернуть приложение в кластере и применить конфигурацию istio, находясь в папке istio-conf выполнить(поправьте скрипт, указав адрес до вашего репозитория!):
 - указываем адрес до конфига, посмотрев его в lens (кластер\general), например "/Users/a.pashchenko/.kube/config"
   ```  
   $ kubectl --kubeconfig /Users/a.pashchenko/.kube/config get ns
   ```
   ```
   $ kubectl --kubeconfig /Users/a.pashchenko/.kube/config apply -f /Volumes/Data/Repositories/security-istio-demo/istio-conf/securityIstioDemoApplication.yaml
   ```
   ```
   $ kubectl --kubeconfig /Users/a.pashchenko/.kube/config apply -f /Volumes/Data/Repositories/security-istio-demo/istio-conf/securityIstioDemoApplicationRequestAuthentication.yaml
   ```
   ```
   $ kubectl --kubeconfig /Users/a.pashchenko/.kube/config apply -f /Volumes/Data/Repositories/security-istio-demo/istio-conf/ingressGateway.yaml
   ```
   ```
   $ kubectl --kubeconfig /Users/a.pashchenko/.kube/config apply -f /Volumes/Data/Repositories/security-istio-demo/istio-conf/virtualService.yaml
   ```
   ```
   $ kubectl --kubeconfig /Users/a.pashchenko/.kube/config apply -f /Volumes/Data/Repositories/security-istio-demo/istio-conf/authorizationPolicy.yaml
   ```

7. Делаем порт-форвардинг пода istio-ingressgateway (не приложения!) и открываем Swagger:
   http://localhost:4041/demo/swagger-ui/index.html#/
   Нужно отключить в браузере cors проверки или использовать curl

8. Тестируем открытый метод через curl или вызвав в Swagger'е:
   ```
   $ curl -X GET http://localhost:4041/demo/public
   ```
   получаем ответ: "Открытый API метод!"

   Тестируем закрытый метод через curl или вызвав в Swagger'е:
   ```
   $ curl -X GET http://localhost:4041/demo/private
   ```
   получаем ответ: "RBAC: access denied"

9. Получаем доступ в закрытую зону, тестируем метод открытый для пользователя с ролью "ROLE_USER" или "ROLE_ADMIN":
 - Получаем токен пользователя test-user:
   ```
   $  curl -X POST -d "client_id=istio" -d "username=test-user" -d "password=test-user" -d "grant_type=password" "http://localhost:4040/realms/istio/protocol/openid-connect/token"
   ```
- Выполняем запрос с подставленным токеном:
   ```
   $  curl -X GET http://localhost:4041/demo/private -H "Authorization: Bearer ****"
   ```
  получаем ответ: "Закрытый API метод, требующий аутентификации под пользователем с ролью ROLE_USER, ROLE_ADMIN!"

   Важно чтобы jwksUri в securityIstioDemoApplicationRequestAuthentication.yaml был внутренний для кластера ip этого же кейклока!

   Список литературы:
   https://www.infracloud.io/blogs/request-level-authentication-authorization-istio-keycloak/



