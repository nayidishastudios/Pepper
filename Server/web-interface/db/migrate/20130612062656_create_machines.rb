class CreateMachines < ActiveRecord::Migration
  def change
    create_table :machines do |t|
      t.string :machine_id
      t.string :name
      t.integer :school_id
      t.text :ssl_key

      t.timestamps
    end
  end
end
