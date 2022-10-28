# Ingegneria dei dati - Hw3
A json format dataset of all the wikitables parsed. It contains about 550K tables. They are exported from a MongoDB collection through the mongoexport command
(Each line of the file is a json table structured like this)
- http://downloads.dbpedia.org/wiki-archive/downloads-2016-10.html
- https://www.dbpedia.org/blog/new-dbpedia-release-2016-10/
- http://downloads.dbpedia.org/2016-10/

Abbiamo preso il corpus e tramite un API abbiamo creato un metodo override con cui abbiamo effettuato il deserialize dei file.json, 
in questo modo ci siamo estratti i dati d'interesse quali il contesto, il numero di righe, il numero di colonne, l'id della tabella, le colonne della tabella inclusi i nomi delle colonne e i dati associati ad ognuna.

Abbiamo quindi dovuto concatenare i dati nella colonna con ;; e trovare degli analyzer che permettessero di indicizzare ogni dato nelle celle di una colonna.

E' stato necessario attraversare tutta la tabella e con la prima passata controllo il nome delle colonne per poi associare ad ognuna di esse il contenuto delle celle relative, il tutto salvando il contenuto delle celle in una mappa.

Alcune tabelle non avevano le colonne quindi gli abbiamo assegnato alla tabella, una sola colonna cui nome è l'indice della colonna e i dati della colonna sono i dati contenuti nella tabella

Abbiamo assunto che esiste un solo header per ogni tabella

L'indicizzazione dura pochissimo così come il searching quindi abbiamo generato più thread per gestire le porzioni delle diverse tabelle