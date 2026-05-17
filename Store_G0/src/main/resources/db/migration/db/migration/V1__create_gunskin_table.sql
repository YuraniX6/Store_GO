CREATE TABLE gunSkin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    gunname VARCHAR(255) NOT NULL,
    skinname VARCHAR(255) NOT NULL,
    condicion_arma VARCHAR(255) NOT NULL,
    collection VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    PRIMARY KEY (id)
);