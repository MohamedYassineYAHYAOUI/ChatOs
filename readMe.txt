##Code

Fichiers sources : fr/uge/net/tcp


- Client : fr/uge/net/tcp/client
- Serveur : fr/uge/net/tcp/server
- Lecture de Trame : fr/uge/net/tcp/frameReaders
- Codes de paquet - Génération de buffer: fr/uge/net/tcp/responses
- Visitor : fr/uge/net/tcp/visitor
- Le rapport technique explique les différents emplacement et structures des fichiers 


##Génération des exécutables

On peut générer les exécutables en faisant : 
	ant jar

cela permet de créer un répertoire exe avec les jar : Client.jar et Server.jar

##Jar exécutables : Dans ChatOS


Jar de serveur : jar/Server.jar
Jar de client : jar/Client.jar


##Usage

les éxcutables se trouve dans le répertoire jar ou dans le répertoire exe généré 


Pour lancer un serveur : 
	java -jar Server.jar [Port]



Pour lancer un Client : 
	java -jar client.jar [user_login] localhost [Port]




##documentation

génération de documentation : 
	ant javadoc

Manuel utilisateur : ChatOS/guide/manuel_utilisateur.pdf
Rapport technique : ChatOS/guide/rapport_ChatOS.pdf
RFC : ChatOS/guide/RFC.pdf




##Javadoc

il faut lancer le fichier index.html pour accéder au JavaDoc : ChatOS/doc/index.html
Pour générer la documentation, on peut faire avec ANT : ant javadoc 