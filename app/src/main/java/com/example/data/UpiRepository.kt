package com.example.data

import kotlinx.coroutines.flow.Flow

class UpiRepository(private val upiDao: UpiDao) {

    val allPayments: Flow<List<UpiPayment>> = upiDao.getAllPayments()

    val allBudgets: Flow<List<CategoryBudget>> = upiDao.getAllBudgets()

    // Extended Budgets
    val allExtendedBudgets: Flow<List<ExtendedBudget>> = upiDao.getAllExtendedBudgets()

    // Group / Family Members
    val allMembers: Flow<List<GroupMember>> = upiDao.getAllMembers()

    // Shared Saving Goals
    val allGoals: Flow<List<SharedGoal>> = upiDao.getAllGoals()

    // Expense Splits (Split Bills)
    val allSplitBills: Flow<List<SplitBill>> = upiDao.getAllSplitBills()

    // Bill Owes
    val allBillOwes: Flow<List<BillOwe>> = upiDao.getAllBillOwes()

    suspend fun insertPayment(payment: UpiPayment) {
        upiDao.insertPayment(payment)
    }

    suspend fun deletePayment(payment: UpiPayment) {
        upiDao.deletePayment(payment)
    }

    suspend fun deletePaymentById(id: Long) {
        upiDao.deletePaymentById(id)
    }

    suspend fun insertBudget(budget: CategoryBudget) {
        upiDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: CategoryBudget) {
        upiDao.deleteBudget(budget)
    }

    // Extended Budgets
    suspend fun insertExtendedBudget(budget: ExtendedBudget) {
        upiDao.insertExtendedBudget(budget)
    }

    suspend fun deleteExtendedBudget(budget: ExtendedBudget) {
        upiDao.deleteExtendedBudget(budget)
    }

    suspend fun deleteExtendedBudgetById(id: Long) {
        upiDao.deleteExtendedBudgetById(id)
    }

    // Family / Group Members
    suspend fun insertMember(member: GroupMember): Long {
        return upiDao.insertMember(member)
    }

    suspend fun deleteMember(member: GroupMember) {
        upiDao.deleteMember(member)
    }

    suspend fun deleteMemberById(id: Long) {
        upiDao.deleteMemberById(id)
    }

    // Shared Saving Goals
    suspend fun insertGoal(goal: SharedGoal) {
        upiDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: SharedGoal) {
        upiDao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(id: Long) {
        upiDao.deleteGoalById(id)
    }

    // Expense Splits (Split Bills)
    suspend fun insertSplitBill(bill: SplitBill, owes: List<BillOwe>) {
        val billId = upiDao.insertSplitBill(bill)
        owes.forEach { owe ->
            upiDao.insertBillOwe(owe.copy(billId = billId))
        }
    }

    suspend fun deleteSplitBill(bill: SplitBill) {
        upiDao.deleteSplitBill(bill)
        upiDao.deleteOwesByBillId(bill.id)
    }

    suspend fun deleteSplitBillById(id: Long) {
        upiDao.deleteSplitBillById(id)
        upiDao.deleteOwesByBillId(id)
    }

    // Bill Owes
    suspend fun updateOweSettlement(id: Long, isSettled: Boolean) {
        upiDao.updateOweSettlement(id, isSettled)
    }
}

