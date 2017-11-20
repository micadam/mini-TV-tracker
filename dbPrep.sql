
CREATE DATABASE tvbase;
USE tvbase;

CREATE USER 'tvtracker'@'localhost' IDENTIFIED BY 'tvpassword';
GRANT ALL ON tvbase.* TO 'tvtracker'@'localhost' IDENTIFIED BY 'tvpassword';



DROP TABLE IF EXISTS Aktor;
CREATE TABLE Aktor (
nazwisko varchar(100),
PRIMARY KEY(nazwisko));

DROP TABLE IF EXISTS Uzytkownik;
CREATE TABLE Uzytkownik (
userID INTEGER NOT NULL AUTO_INCREMENT,
nazwa varchar(100),
PRIMARY KEY(userID));

DROP TABLE IF EXISTS SerialTV;
CREATE TABLE SerialTV (
serialID INTEGER NOT NULL,
tytul varchar(100),
stacja varchar(100),
opis varchar(1000),
statusSerialu varchar(100),
PRIMARY KEY(serialID));

DROP TABLE IF EXISTS Odcinek;
CREATE TABLE Odcinek (
odcinekID INTEGER NOT NULL AUTO_INCREMENT,
serialID INTEGER NOT NULL,
numerWSerialu INTEGER,
numerWSezonie INTEGER,
numerSezonu INTEGER,
tytul varchar(100),
dataPremiery DATE,
opis varchar(1000),
PRIMARY KEY (odcinekID),
UNIQUE(serialID, numerSezonu, numerWSezonie),
FOREIGN KEY (serialID) REFERENCES SerialTV(serialID));

DROP TABLE IF EXISTS AktorWSerialu;
CREATE TABLE AktorWSerialu  (
serialID INTEGER NOT NULL,
nazwisko varchar(100),
rola varchar(100),
FOREIGN KEY (serialID) REFERENCES SerialTV(serialID),
FOREIGN KEY (nazwisko) REFERENCES Aktor(nazwisko));

DROP TABLE IF EXISTS SerialUzytkownika;
CREATE TABLE SerialUzytkownika(
serialID INTEGER NOT NULL,
userID INTEGER NOT NULL,
FOREIGN KEY(serialID) REFERENCES SerialTV(serialID),
FOREIGN KEY(userID) REFERENCES Uzytkownik(userID));

DROP TABLE IF EXISTS OdcinekObejrzany;
CREATE TABLE OdcinekObejrzany (
odcinekID INTEGER NOT NULL,
userID INTEGER NOT NULL,
dataObejrzenia DATE,
ocenaUzytkownika INTEGER,
FOREIGN KEY (odcinekID) REFERENCES Odcinek(odcinekID),
FOREIGN KEY (userID) REFERENCES Uzytkownik(userID),
PRIMARY KEY(odcinekID, userID));


SET @recentShow = 0;
DROP PROCEDURE IF EXISTS addShow;
DELIMITER //
CREATE PROCEDURE addShow(ID INT, title varchar(100), network varchar(100), description varchar(1000), showStatus varchar(100))
BEGIN
    INSERT INTO SerialTV (serialID, tytul, stacja, opis, statusSerialu) VALUES (ID, title, network, description, showStatus);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS addEpisode;
DELIMITER //
CREATE PROCEDURE addEpisode(ID INT, title varchar(100), numberInShow INT, numberInSeason INT, seasonNumber INT, airdate DATE, description varchar(1000))
BEGIN
	INSERT INTO Odcinek(serialID, tytul, numerWSerialu, numerWSezonie, numerSezonu, dataPremiery, opis) VALUES
						(ID, title, numberInShow, numberInSeason, seasonNumber, airdate, description);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS addShowForUser;
DELIMITER //
CREATE PROCEDURE addShowForUser(userID INT, showID INT)
BEGIN
	INSERT INTO SerialUzytkownika(userID, serialID) VALUES (userID, showID);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getShows;
DELIMITER //
CREATE PROCEDURE getShows(thisUserID INT)
BEGIN
	SELECT s1.serialID, tytul, stacja, opis, statusSerialu,
		(SELECT COUNT(*) FROM SerialTV AS s2 INNER JOIN Odcinek AS o ON s2.serialID = o.serialID WHERE s1.serialID = s2.serialID) AS epsTotal,
        (SELECT COUNT(*) FROM SerialTV AS s2 INNER JOIN Odcinek AS o ON s2.serialID = o.serialID
			INNER JOIN OdcinekObejrzany AS oo ON o.odcinekID = oo.odcinekID WHERE s1.serialID = s2.serialID AND oo.userID =thisUserID) AS epsWatched,
		(SELECT AVG(ocenaUzytkownika) FROM OdcinekObejrzany AS oo INNER JOIN Odcinek AS o ON oo.odcinekID = o.odcinekID
			INNER JOIN SerialTV AS s ON s.serialID = o.serialID WHERE s.serialID = s1.serialID AND oo.userID = thisUserID) AS rating
		FROM SerialTV as s1 INNER JOIN SerialUzytkownika AS su ON su.serialID = s1.serialID WHERE su.userID = thisUserID
        ORDER BY tytul;
END//
DELIMITER ;

CALL getShows(1);

DROP PROCEDURE IF EXISTS getEpisodesForShow;
DELIMITER //
CREATE PROCEDURE getEpisodesForShow(showID INT, thisUserID INT)
BEGIN
	SELECT numerSezonu, numerWSezonie, tytul, opis, dataPremiery, (o.odcinekID IN
		(SELECT ou.odcinekID FROM OdcinekObejrzany AS ou WHERE ou.userID = thisUserID)) AS watched, o.odcinekID, ocenaUzytkownika FROM Odcinek AS o
        LEFT JOIN OdcinekObejrzany AS oo ON o.odcinekID = oo.odcinekID
        WHERE o.serialID = showID ORDER BY numerSezonu, numerWSezonie;

END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getLatestEpisodes;
DELIMITER //
CREATE PROCEDURE getLatestEpisodes(thisUserID INT)
BEGIN
	SELECT s.tytul AS showTitle, o.tytul as epTitle, o.dataPremiery as airdate FROM SerialTV AS s INNER JOIN Odcinek AS o ON s.SerialID = o.SerialID
    INNER JOIN SerialUzytkownika AS su ON su.serialID = s.serialID WHERE su.userID = thisUserID ORDER BY o.dataPremiery DESC LIMIT 50;
END//

DROP PROCEDURE IF EXISTS episodeWatched;
DELIMITER //
CREATE PROCEDURE episodeWatched(episodeID INT, userID INT, rating INT)
BEGIN
	INSERT INTO OdcinekObejrzany(odcinekID, userID, dataObejrzenia, ocenaUzytkownika) VALUES
		(episodeID, userID, NOW(), rating);
END//
DELIMITER ;

SELECT * FROM OdcinekObejrzany;
DELETE FROM OdcinekObejrzany;

DROP PROCEDURE IF EXISTS episodeUnwatched;
DELIMITER //
CREATE PROCEDURE episodeUnwatched(episodeID INT, thisUserID INT)
BEGIN
	DELETE FROM OdcinekObejrzany WHERE OdcinekObejrzany.odcinekID = episodeID AND OdcinekObejrzany.userID = thisUserID;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS deleteShowForUser;
DELIMITER //
CREATE PROCEDURE deleteShowForUser(userID INT, showID INT)
BEGIN
	DELETE FROM SerialUzytkownika where SerialUzytkownika.userID = userID AND SerialUzytkownika.serialID = showID;
END//
DELIMITER ;

DROP TRIGGER IF EXISTS deleteUnwatchedShows;
DELIMITER //
CREATE TRIGGER deleteUnwatchedShows AFTER DELETE ON SerialUzytkownika
FOR EACH ROW
BEGIN
	IF OLD.SerialID NOT IN (SELECT SerialID FROM SerialUzytkownika) THEN
		DELETE FROM AktorWSerialu WHERE AktorWSerialu.SerialID = OLD.SerialID;
		DELETE FROM OdcinekObejrzany WHERE OdcinekObejrzany.OdcinekID IN (SELECT OdcinekID FROM Odcinek WHERE Odcinek.SerialID = OLD.SerialID);
		DELETE FROM Odcinek WHERE Odcinek.SerialID = OLD.SerialID;
        DELETE FROM SerialTV WHERE SerialTV.SerialID = OLD.SerialID;
	END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS addActorForShow
DELIMITER //
CREATE PROCEDURE addActorForShow(showID INT, actorName varchar(100), role varchar(100))
BEGIN
	IF actorName NOT IN (SELECT nazwisko FROM Aktor) THEN
		INSERT INTO Aktor VALUES (actorName);
	END IF;
    INSERT INTO AktorWSerialu VALUES (showID, actorName, role);
    
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getActorsForShow
DELIMITER //
CREATE PROCEDURE getActorsForShow(showID INT)
BEGIN
	SELECT nazwisko, rola FROM AktorWSerialu AS aws WHERE aws.serialID = showID;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getTitleForShowID
DELIMITER //
CREATE PROCEDURE getTitleForShowID(showID INT)
BEGIN
	SELECT tytul FROM SerialTV AS s WHERE s.serialID = showID;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS getUsers;
DELIMITER //
CREATE PROCEDURE getUsers()
BEGIN
	SELECT userID, nazwa FROM Uzytkownik;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS addUser;
DELIMITER //
CREATE PROCEDURE addUser(nazwaIn varchar(100))
BEGIN
	INSERT INTO Uzytkownik(nazwa) VALUES (nazwaIn);
    SELECT LAST_INSERT_ID() AS id;
END//
DELIMITER ;

SELECT * FROM Uzytkownik;

SET autocommit=1;
DELETE FROM Odcinek;
DELETE FROM SerialTV;
SELECT * FROM SerialTV;




START TRANSACTION;

SET SQL_SAFE_UPDATES=0;

SELECT SerialTV.tytul, Odcinek.* FROM SerialTV INNER JOIN Odcinek ON SerialTV.serialID = Odcinek.serialID ORDER BY SerialTV.tytul, numerSezonu, numerWSezonie;

SHOW FULL PROCESSLIST;