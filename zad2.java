class zad2 {
    public static void main(String[] args) {
        int a = 0, b = 0;
        try
        {
            a = Integer.parseInt(args[1]);
            b = Integer.parseInt(args[2]);
        }
        catch(NumberFormatException exc)
        {
            System.out.println("NumberFormatException: second and third arguments are not numbers");
        }
        catch (IndexOutOfBoundsException outExc)
        {
            System.out.println("IndexOutOfBoundsException: Not enough arguments");
        }

        if( a == b)
        {
            System.out.println("Empty substring");
        }
        else if( a < b )
        {
            try
            {
                System.out.println(args[0].substring(a, b+1));
            }
            catch(NullPointerException ptrExc)
            {
                System.out.println("Something went wrong with pointers, lul");
            }
            catch(IndexOutOfBoundsException outExc)
            {
                System.out.println("Out of boundary, end of substring beyond original string");
            }
        }
        else
        {
            System.out.println("Out of boundary, start is higher than end");
        }
    }
}