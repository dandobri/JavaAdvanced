package info.kgeorgiy.ja.dobris.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {
    public<T> Stream<T> getData(List<Student> students, Function<Student, T> f) {
        return students.stream().map(f);
    }
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getData(students, Student::getFirstName).toList();
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getData(students, Student::getLastName).toList();
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getData(students, Student::getGroup).toList();
    }
    final Function<Student, String> fullName = student -> student.getFirstName() + " " + student.getLastName();
    @Override
    public List<String> getFullNames(List<Student> students) {
        return getData(students, fullName).toList();
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getData(students, Student::getFirstName).collect(Collectors.toSet());
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.isEmpty() ? "" :
                Collections.max(students, Comparator.comparing(Student::getId)).getFirstName();
    }

    public List<Student> sortData(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator).toList();
    }
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortData(students, Comparator.comparing(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortData(students, Comparator.comparing(Student::getLastName).
                thenComparing(Student::getFirstName).
                thenComparing(Comparator.comparing(Student::getId).reversed()));
    }
    public<T> Stream<Student> filterData(Collection<Student> students, T name, Function<Student, T> f, Comparator<Student> comparator) {
        return students.stream().filter(e -> f.apply(e).equals(name)).sorted(comparator);
    }
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterData(students, name, Student::getFirstName,
                Comparator.comparing(Student::getLastName)).toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterData(students, name, Student::getLastName,
                Comparator.comparing(Student::getFirstName)).toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterData(students, group, Student::getGroup,
                Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName)).toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filterData(students, group, Student::getGroup, Comparator.comparing(Student::getLastName))
                .collect(Collectors.toMap(Student::getLastName,
                        Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
}
