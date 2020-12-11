
import java.util.List;
import java.util.Scanner;

/**
 * Created by Lisa Ramel
 * Date: 2020-12-04
 * Time: 14:03
 * Project: Preeschool
 * Copywrite: MIT
 */
public class Main {

    private final Database d = new Database();

    private AttendanceDAO attendanceDAO = d;
    private DatabaseDAO databaseDAO = d;
    private PersonDAO personDAO = d;

    private Scanner scan = new Scanner(System.in);

    private States state;

/*
1. Välj Vårdnadshavare eller Pedagog ex (1,2)

Vårdnadshavare
1. Välj barn
1. Registerara frånvaro
2. Anmäla omsorgstider
3. Visa pedagogers uppgifter

Pedagog
1. Lägga till frånvaro
2. Registrera nytt barn

 */


    public Main() throws InterruptedException {
        state = States.LOGIN;


        state = States.LOGIN;
        state.output(null);

        int input = scan.nextInt();


        while (true) {
            if (input == 1) {
                Thread.sleep(1000);
                caregiverView(input);
                state = States.LOGIN;
                state.output(null);
                input = scan.nextInt();
                //break;
            } else if (input == 2) {
                Thread.sleep(1000);
                educatorView(input);
                state = States.LOGIN;
                state.output(null);
                input = scan.nextInt();
                //break;
            } else if (input == 3) {
                state = States.SHUT_DOWN;
                state.output(null);
                saveAllFiles();
                break;
            } else {
                System.out.println("Ogiltigt kommando, var god försök igen.");
                input = scan.nextInt();
            }
        }
    }

    public Caregiver lookupCaregiver(String name) {
        Caregiver caregiver = personDAO.getCaregiver(name);
        while (caregiver == null) {
            System.out.println("Var god försök igen: ");
            name = scan.next();
            caregiver = personDAO.getCaregiver(name);
        }
        return caregiver;
    }

    public void caregiverView(int input) throws InterruptedException {
        String name;
        //Om användaren valde att logga in som vårdnadshavare (1)

        state = States.USERNAME;
        state.output(null);
        name = scan.next();
        Caregiver caregiver = lookupCaregiver(name);
        while (true) {
            Child child;
            child = caregiver.getChildren().get(0);

            if (caregiver.getChildren().size() > 1) {

                Thread.sleep(1000);
                state = States.CAREGIVER;
                state.output(caregiver);

                // väljer barn
                input = scan.nextInt();
                //Om användaren valde ett barn (1)
                if (input <= caregiver.getChildren().size()) {
                    child = caregiver.getChildren().get(input - 1);
                }
            }

            Thread.sleep(1000);
            state = States.CHOSE_CHILD;
            state.output(child);

            input = scan.nextInt();

            //Om användaren valde omsorgstider (1)
            if (input == 1) {

                Thread.sleep(1000);
                state = States.CHILD_ATTENDANCE;
                state.output(child);
                state.addCaringTime(child, scan);

            }

            //Om användaren valde frånvaro (2)
            else if (input == 2) {
                Thread.sleep(1000);
                state = States.CHILD_ABSENCE;
                addAbsenseToday(child);
            }

            //Om användaren valde kontaktuppgifter (3)
            else if (input == 3) {

                Thread.sleep(1000);
                state = States.EDUCATOR_INFO;
                List<Educator> educatorList = personDAO.getEducatorList();
                state.output(educatorList);
           
            } else if (input == 4) {
                Thread.sleep(1000);
                state = States.LOG_OUT;
                state.output(caregiver);
                break;
            }

            else {
                System.out.println("Okänt kommando, var god försök igen.");

            }
        }

    }


    public void educatorView(int input) throws InterruptedException {

        String name;
        String firstName;

        //Om användaren valde att logga in som pedagog (2)

        state = States.USERNAME;
        state.output(null);

        name = scan.next();
        Educator educator = personDAO.getEducator(name);
        while (educator == null) {
            System.out.println("Var god försök igen: ");
            name = scan.next();
            educator = personDAO.getEducator(name);
        }

        while (true) {

            Thread.sleep(1000);
            state = States.EDUCATOR;
            state.output(educator);
            input = scan.nextInt();

            //Om användaren valde att registrera frånvaro för ett barn
            if (input == 1) {
                Thread.sleep(1000);
                state = States.EDUCATOR_ABSENCE;

                List<Child> childList = personDAO.getChildList();
                state.output(personDAO.getChildList());
                input = scan.nextInt();

                //Registrerar frånvaro på barn
                if (input <= childList.size()) {

                    Thread.sleep(1000);
                    state = States.CHILD_ABSENCE;
                    Child child = childList.get(input - 1);
                    addAbsenseToday(child);
                }


                //Om användaren vill lägga till ett barn
            } else if (input == 2) {

                Thread.sleep(1000);
                state = States.REGISTER_CHILD;
                state.output(null);
                firstName = scan.next();

                boolean foundCaregiver = false;

                //Om vårdnadshavaren redan finns i systemet, läggs
                //ett nytt barn läggs till till den redan exsisterande vårdnadshavaren
                for (Caregiver caregiver : personDAO.getCaregiverList()) {
                    if (caregiver.getFirstName().equalsIgnoreCase(firstName)) {
                        System.out.println("Det nya barnet kommer att registreras på den redan " +
                                "\nexisterande vårdnadshavaren " + caregiver.getFirstName() + " " + caregiver.getLastName());

                        Child child = state.registerNewChild(scan);
                        databaseDAO.addChild(child);
                        child.addCaregiver(caregiver);
                        caregiver.addChildren(child);
                        attendanceDAO.addChildInAttendance(child);
                        foundCaregiver = true;
                    }
                }

                //Om det är en ny vårdnadshavare så adderas en ny vårdnadshavare
                //och ett nytt barn läggs till och kopplas till den nya vårdnadshavaren
                if (!foundCaregiver) {

                    Caregiver caregiver = state.addCaregiverToNewChild(scan, firstName);
                    databaseDAO.addCaregiver(caregiver);
                    Child child = state.registerNewChild(scan);
                    databaseDAO.addChild(child);
                    child.addCaregiver(caregiver);
                    caregiver.addChildren(child);
                    attendanceDAO.addChildInAttendance(child);
                }
                saveAllFiles();

            }
            //Om användaren vill skriva ut närvarolistor
            else if (input == 3) {
              
                Thread.sleep(1000);
                state = States.ATTENDANCE;
                state.output(null);
                input = scan.nextInt();
                if (input == 1) {
                    Thread.sleep(1000);
                    state = States.PRINT_ALL;
                    state.output(attendanceDAO.getAttendanceToday());
                } else if (input == 2) {
                    Thread.sleep(1000);
                    state = States.PRINT_PRESENT;
                    state.output(attendanceDAO.getAttendanceToday());
                } else if (input == 3) {
                    Thread.sleep(1000);
                    state = States.PRINT_ABSENT;
                    state.output(attendanceDAO.getAttendanceToday());
                }
            }

            // om användaren vill se ett barns omsorgstider
            else if(input == 4){
                state = States.SEE_CARINGTIMES;
                state.output(personDAO.getChildList());
                input = scan.nextInt();
                state.showCaringTimes(personDAO.getChildList().get(input-1));

            }

            else if (input == 5) {
                state = States.CAREGIVER_INFO;
                state.output(null);

                name = scan.next();
                List<Child> childList = personDAO.getChildList();
                for (Child child : childList) {
                    if (name.equalsIgnoreCase(child.getFirstName()) || name.equalsIgnoreCase(child.getLastName())) {
                        state = States.CAREGIVER_INFO_PRINT;
                        state.output(child);
                    }
                }
            }

            //Om användaren valde att Logga ut (5)

            else if (input == 6) {
                state = States.LOG_OUT;
                state.output(educator);
                break;

            } else {
                System.out.println("Okänt kommando, var god försök igen.");
            }

        }
    }
    public void addAbsenseToday(Child child){
        state.output(child);
        attendanceDAO.addAbsence(child);
        d.serialize(attendanceDAO.getAttendanceToday(), SerFiles.ATTENDANCE.serFiles);
    }

    public void saveAllFiles(){
        attendanceDAO.addAttendanceTodayInList(attendanceDAO.getAttendanceToday());
        d.serialize(attendanceDAO.getAttendanceList(), SerFiles.LIST_OF_ATTENDANCES.serFiles);
        d.serialize(attendanceDAO.getAttendanceToday(), SerFiles.ATTENDANCE.serFiles);
        d.serialize(personDAO.getChildList(), SerFiles.CHILDREN.serFiles);
        d.serialize(personDAO.getEducatorList(), SerFiles.EDUCATOR.serFiles);
    }

    public static void main(String[] args) throws InterruptedException {
        Main main = new Main();
    }
}


