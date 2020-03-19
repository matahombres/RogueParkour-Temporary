/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rogueparkour_placeholder;
import cl.omegacraft.kledioz.rparkour.Main;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
/**
 *
 * @author Admin
 */
public class RogueParkour_placeholder  extends PlaceholderExpansion{ 

    /**
     * This method should always return true unless we
     * have a dependency we need to make sure is on the server
     * for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return "matahombress";
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getName(){
        return "RogueParkour-temporary";
    }
    @Override
    public String getIdentifier(){
        return "RogueParkour-temporary";
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return "1.2";
    }
    
    @Override
    public String getRequiredPlugin(){
        return "RogueParkour";
    }
    
    public String getPlugin() {
        return null;
    }
    
    public FileConfiguration data;
    public ArrayList<String> id;
    public HashMap<String,String> uuid_name;
    public HashMap<String,Integer> uuid_score;
    public ArrayList<String> uuid_score_ordenado;
    
    public FileConfiguration general_config=Main.get().getConfig();
    @Override
    public String onRequest(final OfflinePlayer player, final String identifier){
        boolean mysql_enable=general_config.getBoolean("MYSQL.enabled");
        data=Main.newConfigz;
        //%RogueParkour-temporary_top_<number>;<type>%
        //type==score or name
        //%RogueParkour-temporary_get_<player>%
        if(identifier.startsWith("top")){
            uuid_name=new HashMap<>();
            uuid_score=new HashMap<>();
            id=new ArrayList<>();
            uuid_score_ordenado=new ArrayList<>();
            if(mysql_enable){
                try {
                    //Metodo sql
                    connection sql=new connection(general_config.getString("MYSQL.ip"), general_config.getString("MYSQL.port"), general_config.getString("MYSQL.database"), general_config.getString("MYSQL.user"), general_config.getString("MYSQL.password"));
                    sql.openConnection();
                    Statement st=sql.getConnection().createStatement();
                    ResultSet rs=st.executeQuery("SELECT * FROM `RPScore`");
                    while(rs.next()){
                        String uuid=rs.getString("player");
                        OfflinePlayer pl=getOfflinePlayer(uuid,true);
                        String name;
                        int score=rs.getInt("score");
                        if(pl==null){
                            name=uuid;
                        }else{
                            name=pl.getName();;
                        }
                        uuid_name.put(uuid, name);
                        uuid_score.put(uuid, score);
                        id.add(uuid);
                        
                    }
                    sql.closeConnection();
                } catch (SQLException ex) {
                    Logger.getLogger(RogueParkour_placeholder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                //Metodo fichero
                Set<String> all_data= data.getKeys(false);
                for(String uuid : all_data){
                    uuid_name.put(uuid, data.getString(uuid+".name"));
                    uuid_score.put(uuid, data.getInt(uuid+".highscore"));
                    id.add(uuid);
                }
            }
            //Ordenar los hashmap
            for(int i=0;i<uuid_score.size();i++){
                int max=0;
                int orden=0;
                for(int s=0;s<uuid_score.size();s++){
                    if(uuid_score.get(id.get(s))>max){
                        if(!uuid_score_ordenado.contains(id.get(s))){
                            max=uuid_score.get(id.get(s));
                            orden=s;
                        }
                    }
                }
                uuid_score_ordenado.add(id.get(orden));
            }
            //Aplicar los datos
            String[] string=identifier.split("top_")[1].split(";");
            int number=Integer.valueOf(string[0]);
            int select=number-1;
            String type="null";
            if(string.length>1){
                type=string[1];
            }
            if(uuid_score_ordenado.size()>=number&&number>0){
                String uuid=uuid_score_ordenado.get(select);
                if(type.equalsIgnoreCase("name")){
                    return uuid_name.get(uuid);
                }else{
                    return String.valueOf(uuid_score.get(uuid));
                }
            }else{
                return "";
            }
        }else if(identifier.startsWith("get_")){
            String plName=identifier.split("get_")[1];
            OfflinePlayer p=getOfflinePlayer(plName,false);
            if(player==null){
                p=getOfflinePlayer(plName,false);
                if(p==null){
                    return "PLAYER_NOT_FOUND";
                }
            }
            return String.valueOf((int)cl.omegacraft.kledioz.rparkour.API.getScore((Player)player));
        }else{
            return null;
        }
    }    
    public static OfflinePlayer getOfflinePlayer(final String playerStr, final boolean isUUID) {
        OfflinePlayer[] offlinePlayers;
        for (int length = (offlinePlayers = Bukkit.getOfflinePlayers()).length, i = 0; i < length; ++i) {
            final OfflinePlayer p = offlinePlayers[i];
            if (isUUID && p.getUniqueId().toString().equalsIgnoreCase(playerStr)) {
                return p;
            }
            if (p.getName().equalsIgnoreCase(playerStr)) {
                return p;
            }
        }
        return null;
    }
    
}
