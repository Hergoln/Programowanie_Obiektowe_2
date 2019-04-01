import java.util.*;
// question about next game should be a little  bit further
class zad3 {
    public static void main(String[] args) {
        int randVal = (int)(Math.random()*100);
        Scanner input = new Scanner(System.in);
        System.out.println("Number generated, make your guess");
        int val, iter = 1;
        do
        {
            try {
                val = input.nextInt();
                if (val > randVal) {
                    System.out.println("Value is higher");
                } else if (val < randVal) {
                    System.out.println("Value is lower");
                } else {
                    input.nextLine();
                    System.out.println("Congratulations, you hit in " + iter + " tries");
                    iter = 1;
                    System.out.println("Do you want to play again? Y(yes)");
                    String inputAns = input.nextLine();
                    if(inputAns.isEmpty() || inputAns.toLowerCase().charAt(0) != 'y')
                    {
                        System.out.println("Thank you for playing");
                        return;
                    }
                    System.out.println("Number generated, make your guess");
                    randVal = (int)(Math.random()*100);
                }
                ++iter;
            }
            catch(InputMismatchException misExc){
                System.out.println("Incorrect input");
                input.nextLine();
            }
            catch(IllegalStateException illExc)
            {
                System.out.println("Scanner is not closed, program is closing");
                return;
            }
        }while(true);
    }
}