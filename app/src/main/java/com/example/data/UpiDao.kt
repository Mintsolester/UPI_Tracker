package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UpiDao {
    // UPI Payments
    @Query("SELECT * FROM upi_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<UpiPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: UpiPayment)

    @Delete
    suspend fun deletePayment(payment: UpiPayment)

    @Query("DELETE FROM upi_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    // Category Budgets (Legacy/Compat)
    @Query("SELECT * FROM category_budgets")
    fun getAllBudgets(): Flow<List<CategoryBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CategoryBudget)

    @Delete
    suspend fun deleteBudget(budget: CategoryBudget)

    // Extended Budgets (Overall, Category, Weekly, Custom)
    @Query("SELECT * FROM extended_budgets ORDER BY startDate DESC")
    fun getAllExtendedBudgets(): Flow<List<ExtendedBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtendedBudget(budget: ExtendedBudget)

    @Delete
    suspend fun deleteExtendedBudget(budget: ExtendedBudget)

    @Query("DELETE FROM extended_budgets WHERE id = :id")
    suspend fun deleteExtendedBudgetById(id: Long)

    // Family / Group Members
    @Query("SELECT * FROM group_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<GroupMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMember): Long

    @Delete
    suspend fun deleteMember(member: GroupMember)

    @Query("DELETE FROM group_members WHERE id = :id")
    suspend fun deleteMemberById(id: Long)

    // Shared Saving Goals
    @Query("SELECT * FROM shared_goals ORDER BY targetDate ASC")
    fun getAllGoals(): Flow<List<SharedGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SharedGoal)

    @Delete
    suspend fun deleteGoal(goal: SharedGoal)

    @Query("DELETE FROM shared_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    // Expense Splitting (Split Bills)
    @Query("SELECT * FROM split_bills ORDER BY timestamp DESC")
    fun getAllSplitBills(): Flow<List<SplitBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitBill(bill: SplitBill): Long

    @Delete
    suspend fun deleteSplitBill(bill: SplitBill)

    @Query("DELETE FROM split_bills WHERE id = :id")
    suspend fun deleteSplitBillById(id: Long)

    // Bill Owes
    @Query("SELECT * FROM bill_owes")
    fun getAllBillOwes(): Flow<List<BillOwe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillOwe(owe: BillOwe)

    @Query("UPDATE bill_owes SET isSettled = :isSettled WHERE id = :id")
    suspend fun updateOweSettlement(id: Long, isSettled: Boolean)

    @Query("DELETE FROM bill_owes WHERE billId = :billId")
    suspend fun deleteOwesByBillId(billId: Long)
}

