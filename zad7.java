import java.util.*;

class  NrTelefoniczny implements Comparable<NrTelefoniczny>{
    int nrKierunkowy, nrTelefonu;

    public NrTelefoniczny(int _nrKierunkowy, int _nrTelefonu){
        nrKierunkowy = _nrKierunkowy;
        nrTelefonu = _nrTelefonu;
    }

    public NrTelefoniczny(NrTelefoniczny old){
        nrKierunkowy = old.nrKierunkowy;
        nrTelefonu = old.nrTelefonu;
    }

    public String toString(){
        return nrKierunkowy + " " + nrTelefonu;
    }

    public int compareTo(NrTelefoniczny compared)
    {
        if(this.nrKierunkowy == compared.nrKierunkowy) {
            if(this.nrTelefonu == compared.nrTelefonu)
                return 0;
            else
                return -1;
        }
        return 1;
    }
}

class Adres{
    int houseNumber;
    String street, city;
    NrTelefoniczny tel;

    public Adres(String _city, String _street, int _houseNumber, int _nrKierunkowy, int _nrTelefoniczny){
        tel = new NrTelefoniczny(_nrKierunkowy, _nrTelefoniczny);
        street = _street;
        city = _city;
        houseNumber = _houseNumber;
    }

    public Adres(String _city, String _street, int _houseNumber, NrTelefoniczny _nrTelefoniczny){
        tel = new NrTelefoniczny(_nrTelefoniczny);
        street = _street;
        city = _city;
        houseNumber = _houseNumber;
    }

    public String Opis(){
        return "city: " + city + ", street: " + street + ", house: " + houseNumber + ", nr.tel:" + tel;
    }
}

abstract class Wpis{
    String name;
    public Wpis(String _name){
        name = _name;
    }

    abstract public String Opis();
}

class Osoba extends Wpis{
    String surName;
    Adres adres;

    public Osoba(String _name, String _surName, Adres _adres){
        super(_name);
        surName = _surName;
        adres = _adres;
    }

    public String Opis(){
        return "My name is " + name + " " + surName + ", from: " + adres.Opis();
    }
}

class Firma extends Wpis{
    Adres adres;

    public Firma(String _name, Adres _adres){
        super(_name);
        adres = _adres;
    }

    public Firma(String _name, String _city, String _street, int _houseNumber, NrTelefoniczny _nrTelefoniczny){
        super(_name);
        adres = new Adres(_city, _street, _houseNumber, _nrTelefoniczny);
    }

    public String Opis(){
        return "Company's name:" + name + ", from: " + adres.Opis();
    }
}

class zad7 {
    public static void main(String[] args) {
        TreeMap<NrTelefoniczny, Wpis> ksiazka = new TreeMap<NrTelefoniczny, Wpis>();

        ksiazka.put(new NrTelefoniczny(1, 1), new Firma("First", "NewYork", "last", 1, new NrTelefoniczny(1, 1)));
        ksiazka.put(new NrTelefoniczny(2, 2), new Osoba("Second", "June", new Adres("Warsaw", "quest", 2, new NrTelefoniczny(2, 2))));
        ksiazka.put(new NrTelefoniczny(3, 3), new Firma("Third", "London", "guest", 3, new NrTelefoniczny(3, 3)));
        ksiazka.put(new NrTelefoniczny(4, 4), new Osoba("Fourth", "July", new Adres("NewYork", "squeak", 4, new NrTelefoniczny(4, 4))));

        //iteration
        try
        {
            Iterator<Map.Entry<NrTelefoniczny, Wpis>> iter = ksiazka.entrySet().iterator();
            while(iter.hasNext()){
                System.out.println(iter.next().getValue().Opis());
            }
        }
        catch (NullPointerException NPEExc){
            System.out.println("huehue");
        }
    }
}