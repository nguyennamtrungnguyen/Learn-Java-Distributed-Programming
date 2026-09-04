package repository;

import entity.Appointment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import util.JPAUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppointmentRepository {

    public boolean addAppointment(Appointment appointment){
        try(EntityManager em = JPAUtil.getEntityManager()){
            em.getTransaction().begin();
            if (appointment.getPatient().getId() == null || appointment.getDoctor().getId() == null){
                em.getTransaction().rollback();
                return false;
            }
            em.persist(appointment);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
//    Liệt kê danh sách lịch khám gồm: Mã số bác sĩ, tên bác sĩ, mã số bệnh nhân, tên bệnh nhân, thời
//    gian khám và trạng thái
    public List<Object[]> getAppointmentDetails(){
        try (EntityManager em = JPAUtil.getEntityManager()){
            String jpql = """
                    select a.doctor.id, a.doctor.fullName, a.patient.id, a.patient.fullName, a.appointmentID.appointmentTime, a.status
                    from Appointment a
                    group by a.doctor.id, a.doctor.fullName, a.patient.id, a.patient.fullName, a.appointmentID.appointmentTime, a.status
                    """;

            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            return query.getResultList();
        }
    }
//
//    Đề: Thống kê số lượng lịch khám theo trạng thái.
//    Hàm: countAppointmentsByStatus(): Map<String, Long>
//    Yêu cầu: Thống kê số lịch theo từng trạng thái PENDING, CONFIRMED, CANCELLED.
    public Map<String, Long> countAppointmentsByStatus(){
        try (EntityManager em = JPAUtil.getEntityManager()){
            String jpql = """
                    select a.status, count (a)
                    from Appointment a
                    group by a.status
                    """;

            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);

            return query.getResultList().stream().collect(Collectors.toMap(
               obj -> obj[0].toString(),
               obj -> (Long) obj[1],
                    (o1, o2) -> o1,
                    LinkedHashMap::new
            ));
        }
    }

//    Đề: Liệt kê bác sĩ có nhiều lịch khám nhất.
//            Hàm: getTopDoctorByAppointments(): List<Object[]>
//    Yêu cầu: Hiển thị mã bác sĩ, tên bác sĩ và số lượng lịch khám.

    public List<Object[]> getTopDoctorByAppointments() {
        try (EntityManager em = JPAUtil.getEntityManager()) {

            String jpql = """
                select a.doctor.id,
                       a.doctor.fullName,
                       count(a)
                from Appointment a
                group by a.doctor.id, a.doctor.fullName
                having count(a) >= all (
                    select count(a2)
                    from Appointment a2
                    group by a2.doctor.id
                )
                """;

            return em.createQuery(jpql, Object[].class)
                    .getResultList();
        }
    }










}




